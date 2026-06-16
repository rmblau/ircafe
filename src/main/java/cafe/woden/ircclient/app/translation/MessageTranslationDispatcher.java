package cafe.woden.ircclient.app.translation;

import cafe.woden.ircclient.app.api.MessageTranslation;
import cafe.woden.ircclient.app.api.UiPort;
import cafe.woden.ircclient.config.IrcProperties;
import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.config.execution.ExecutorConfig;
import cafe.woden.ircclient.model.TargetRef;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.layered.ApplicationLayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/** Schedules configured message translation requests away from the Swing event thread. */
@Component
@ApplicationLayer
@RequiredArgsConstructor
public final class MessageTranslationDispatcher {

  private static final Logger log = LoggerFactory.getLogger(MessageTranslationDispatcher.class);

  private final IrcProperties ircProperties;
  private final MessageTranslationSettingsBus settingsBus;
  private final MessageTranslationBackendRegistry backendRegistry;
  private final MessageLanguageDetector languageDetector;
  private final ObjectProvider<UiPort> uiProvider;

  @Qualifier(ExecutorConfig.TRANSLATION_EXECUTOR)
  private final ExecutorService translationExecutor;

  private volatile InstalledPluginsPort installedPlugins;

  private final AtomicInteger inFlight = new AtomicInteger();

  @Autowired(required = false)
  void setInstalledPlugins(InstalledPluginsPort installedPlugins) {
    this.installedPlugins = installedPlugins;
  }

  public boolean requestIncomingMessageTranslation(
      TargetRef target, Instant at, String fromNick, String messageId, String text) {
    return requestMessageTranslation(target, at, fromNick, messageId, text, "", true);
  }

  public boolean requestManualMessageTranslation(
      TargetRef target,
      Instant at,
      String fromNick,
      String messageId,
      String text,
      String targetLanguage) {
    return requestMessageTranslation(target, at, fromNick, messageId, text, targetLanguage, false);
  }

  private boolean requestMessageTranslation(
      TargetRef target,
      Instant at,
      String fromNick,
      String messageId,
      String text,
      String targetLanguageOverride,
      boolean automatic) {
    IrcProperties.Client.Translation translation = ircProperties.client().translation();
    if (settingsBus != null) {
      translation = settingsBus.get();
    }
    if (!translation.enabled()) {
      logManualSkip(automatic, "translation is disabled");
      return false;
    }
    if (automatic && translation.mode() != IrcProperties.Client.Translation.Mode.AUTO) {
      return false;
    }
    if (target == null || target.isUiOnly()) {
      logManualSkip(automatic, "target is unavailable or UI-only");
      return false;
    }

    String normalizedMessageId = Objects.toString(messageId, "").trim();
    if (normalizedMessageId.isBlank()) {
      logManualSkip(automatic, "message id is blank");
      return false;
    }

    String textToTranslate = Objects.toString(text, "");
    if (textToTranslate.isBlank() || textToTranslate.length() > translation.maxRequestChars()) {
      logManualSkip(
          automatic,
          "message text is blank or exceeds maxRequestChars (length={}, max={})",
          textToTranslate.length(),
          translation.maxRequestChars());
      return false;
    }
    String targetLanguage = firstNonBlank(targetLanguageOverride, translation.targetLanguage());
    if (!shouldTranslateBetween(translation.sourceLanguage(), targetLanguage)) {
      logManualSkip(
          automatic,
          "source and target languages do not require translation (source={}, target={})",
          translation.sourceLanguage(),
          targetLanguage);
      return false;
    }

    Optional<MessageTranslationBackend> backend = backendRegistry.find(translation.backendId());
    if (backend.isEmpty()) {
      logManualSkip(
          automatic, "configured backend is not registered (backend={})", translation.backendId());
      return false;
    }
    if (!tryReserveSlot(translation.maxConcurrentRequests())) {
      logManualSkip(
          automatic,
          "translation concurrency limit reached (max={})",
          translation.maxConcurrentRequests());
      return false;
    }

    MessageTranslationRequest request =
        new MessageTranslationRequest(
            target,
            at,
            fromNick,
            normalizedMessageId,
            textToTranslate,
            translation.sourceLanguage(),
            targetLanguage);
    long requestTimeoutMs = translation.requestTimeoutMs();
    boolean translateUnknownMessages = translation.translateUnknownMessages();
    List<String> detectionLanguageCodes = detectionLanguageCodes(translation);
    try {
      translationExecutor.execute(
          () ->
              runTranslationWithPreflight(
                  backend.get(),
                  request,
                  requestTimeoutMs,
                  automatic,
                  translateUnknownMessages,
                  detectionLanguageCodes));
      if (!automatic) {
        log.info(
            "[translation] scheduled manual request target={} msgid={} backend={} source={} target={}",
            target,
            normalizedMessageId,
            backend.get().backendId(),
            request.sourceLanguage(),
            request.targetLanguage());
      }
      return true;
    } catch (RuntimeException ex) {
      releaseSlot();
      logManualSkip(automatic, "translation executor rejected request: {}", ex.toString());
      return false;
    }
  }

  private void runTranslationWithPreflight(
      MessageTranslationBackend backend,
      MessageTranslationRequest request,
      long timeoutMs,
      boolean suppressSameLanguageResult,
      boolean translateUnknownMessages,
      List<String> detectionLanguageCodes) {
    MessageTranslationRequest preparedRequest =
        prepareTranslationRequest(
            request, suppressSameLanguageResult, translateUnknownMessages, detectionLanguageCodes);
    if (preparedRequest == null) {
      releaseSlot();
      return;
    }
    runTranslation(backend, preparedRequest, timeoutMs, suppressSameLanguageResult);
  }

  private MessageTranslationRequest prepareTranslationRequest(
      MessageTranslationRequest request,
      boolean automatic,
      boolean translateUnknownMessages,
      List<String> detectionLanguageCodes) {
    if (!automatic || !isAutoLanguage(request.sourceLanguage())) {
      return request;
    }
    Optional<String> detectedSourceLanguage =
        detectAutomaticSourceLanguage(
            automatic, request.sourceLanguage(), request.text(), detectionLanguageCodes);
    if (detectedSourceLanguage.isEmpty()) {
      if (!translateUnknownMessages) {
        log.debug(
            "[translation] skipped automatic request because source language is unknown target={} msgid={} targetLanguage={}",
            request.target(),
            request.messageId(),
            request.targetLanguage());
      }
      return translateUnknownMessages ? request : null;
    }
    String detectedLanguage = detectedSourceLanguage.get();
    if (sameLanguage(detectedLanguage, request.targetLanguage())) {
      log.debug(
          "[translation] skipped automatic request because detected language matches target target={} msgid={} language={}",
          request.target(),
          request.messageId(),
          detectedLanguage);
      return null;
    }
    return new MessageTranslationRequest(
        request.target(),
        request.at(),
        request.fromNick(),
        request.messageId(),
        request.text(),
        detectedLanguage,
        request.targetLanguage());
  }

  private void runTranslation(
      MessageTranslationBackend backend,
      MessageTranslationRequest request,
      long timeoutMs,
      boolean suppressSameLanguageResult) {
    boolean completionRegistered = false;
    try {
      CompletionStage<MessageTranslationResult> stage = backend.translate(request);
      if (stage == null) {
        log.warn(
            "[translation] backend returned no completion stage target={} msgid={} backend={}",
            request.target(),
            request.messageId(),
            backend.backendId());
        return;
      }
      stage
          .toCompletableFuture()
          .orTimeout(Math.max(1L, timeoutMs), TimeUnit.MILLISECONDS)
          .whenComplete(
              (result, error) -> {
                try {
                  if (error == null
                      && result != null
                      && !result.translatedText().isBlank()
                      && !shouldSuppressTranslationResult(
                          request, result, suppressSameLanguageResult)) {
                    UiPort ui = uiProvider.getIfAvailable();
                    if (ui != null) {
                      boolean applied =
                          ui.applyMessageTranslation(
                              request.target(),
                              request.at(),
                              toMessageTranslation(backend, request, result));
                      if (!applied) {
                        log.warn(
                            "[translation] UI did not apply translation target={} msgid={} backend={}",
                            request.target(),
                            request.messageId(),
                            backend.backendId());
                      }
                    } else {
                      log.warn(
                          "[translation] UI port unavailable for translation result target={} msgid={} backend={}",
                          request.target(),
                          request.messageId(),
                          backend.backendId());
                    }
                  } else if (error != null) {
                    log.warn(
                        "[translation] backend failed target={} msgid={} backend={} error={}",
                        request.target(),
                        request.messageId(),
                        backend.backendId(),
                        error.toString());
                  }
                } finally {
                  releaseSlot();
                }
              });
      completionRegistered = true;
    } catch (Throwable ex) {
      log.warn(
          "[translation] backend threw before registering completion target={} msgid={} backend={} error={}",
          request.target(),
          request.messageId(),
          backend.backendId(),
          ex.toString());
    } finally {
      if (!completionRegistered) {
        releaseSlot();
      }
    }
  }

  private static boolean shouldSuppressTranslationResult(
      MessageTranslationRequest request,
      MessageTranslationResult result,
      boolean suppressSameLanguageResult) {
    if (!suppressSameLanguageResult) {
      return false;
    }
    return sameLanguage(
        firstNonBlank(result.sourceLanguage(), request.sourceLanguage()),
        firstNonBlank(result.targetLanguage(), request.targetLanguage()));
  }

  private static MessageTranslation toMessageTranslation(
      MessageTranslationBackend backend,
      MessageTranslationRequest request,
      MessageTranslationResult result) {
    String sourceLanguage = firstNonBlank(result.sourceLanguage(), request.sourceLanguage());
    String targetLanguage = firstNonBlank(result.targetLanguage(), request.targetLanguage());
    String provider = firstNonBlank(result.provider(), backend.backendId());
    return new MessageTranslation(
        request.messageId(), result.translatedText(), sourceLanguage, targetLanguage, provider);
  }

  private boolean tryReserveSlot(int maxConcurrentRequests) {
    int max = Math.max(1, maxConcurrentRequests);
    while (true) {
      int current = inFlight.get();
      if (current >= max) {
        return false;
      }
      if (inFlight.compareAndSet(current, current + 1)) {
        return true;
      }
    }
  }

  private void releaseSlot() {
    inFlight.updateAndGet(current -> Math.max(0, current - 1));
  }

  private static boolean shouldTranslateBetween(String sourceLanguage, String targetLanguage) {
    String source = Objects.toString(sourceLanguage, "").trim();
    String target = Objects.toString(targetLanguage, "").trim();
    if (target.isBlank()) {
      return false;
    }
    return source.isBlank() || "auto".equalsIgnoreCase(source) || !sameLanguage(source, target);
  }

  private Optional<String> detectAutomaticSourceLanguage(
      boolean automatic, String sourceLanguage, String text, List<String> detectionLanguageCodes) {
    if (!automatic || !isAutoLanguage(sourceLanguage)) {
      return Optional.empty();
    }
    try {
      return languageDetector.detectLanguageCode(text, detectionLanguageCodes);
    } catch (RuntimeException ignored) {
      return Optional.empty();
    }
  }

  private List<String> detectionLanguageCodes(IrcProperties.Client.Translation translation) {
    return MessageTranslationLanguageCatalog.availableTargets(translation, installedPlugins)
        .stream()
        .map(MessageTranslationLanguage::code)
        .toList();
  }

  private static boolean isAutoLanguage(String value) {
    return "auto".equals(normalizeLanguage(value));
  }

  private static String firstNonBlank(String preferred, String fallback) {
    String value = Objects.toString(preferred, "").trim();
    return value.isBlank() ? Objects.toString(fallback, "").trim() : value;
  }

  private static boolean sameLanguage(String left, String right) {
    String a = normalizeLanguage(left);
    String b = normalizeLanguage(right);
    if (a.isBlank() || b.isBlank() || "auto".equals(a) || "auto".equals(b)) {
      return false;
    }
    if (a.equals(b)) {
      return true;
    }
    return languageBase(a).equals(languageBase(b))
        && !languageBase(a).isBlank()
        && (!a.contains("-") || !b.contains("-"));
  }

  private static String normalizeLanguage(String value) {
    return Objects.toString(value, "").trim().toLowerCase(java.util.Locale.ROOT).replace('_', '-');
  }

  private static String languageBase(String value) {
    int idx = value.indexOf('-');
    return idx < 0 ? value : value.substring(0, idx);
  }

  private static void logManualSkip(boolean automatic, String message, Object... args) {
    if (!automatic) {
      log.warn("[translation] manual request skipped: " + message, args);
    }
  }
}
