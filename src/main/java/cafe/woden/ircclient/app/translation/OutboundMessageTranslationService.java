package cafe.woden.ircclient.app.translation;

import cafe.woden.ircclient.config.IrcProperties;
import cafe.woden.ircclient.config.execution.ExecutorConfig;
import cafe.woden.ircclient.model.TargetRef;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.jmolecules.architecture.layered.ApplicationLayer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/** Coordinates one-off outbound draft translations requested from the input bar. */
@Component
@ApplicationLayer
public class OutboundMessageTranslationService {

  private final MessageTranslationSettingsBus settingsBus;
  private final MessageTranslationBackendRegistry backendRegistry;
  private final ExecutorService translationExecutor;
  private final AtomicInteger inFlight = new AtomicInteger();

  public OutboundMessageTranslationService(
      MessageTranslationSettingsBus settingsBus,
      MessageTranslationBackendRegistry backendRegistry,
      @Qualifier(ExecutorConfig.TRANSLATION_EXECUTOR) ExecutorService translationExecutor) {
    this.settingsBus = Objects.requireNonNull(settingsBus, "settingsBus");
    this.backendRegistry = Objects.requireNonNull(backendRegistry, "backendRegistry");
    this.translationExecutor = Objects.requireNonNull(translationExecutor, "translationExecutor");
  }

  public CompletionStage<MessageTranslationResult> translateDraft(
      TargetRef target, String text, String targetLanguage) {
    IrcProperties.Client.Translation settings = settingsBus.get();
    if (settings == null || !settings.enabled()) {
      return failedFuture(new IllegalStateException("Translation is disabled."));
    }
    if (target == null || target.isUiOnly()) {
      return failedFuture(new IllegalArgumentException("No chat target is active."));
    }

    String body = Objects.toString(text, "");
    if (body.isBlank()) {
      return failedFuture(new IllegalArgumentException("Enter a message before translating."));
    }
    int maxChars = Math.max(1, settings.maxRequestChars());
    if (body.length() > maxChars) {
      return failedFuture(
          new IllegalArgumentException(
              "Message is too long to translate (" + body.length() + "/" + maxChars + ")."));
    }

    String language = normalizeLanguage(targetLanguage);
    if (language.isBlank() || "auto".equals(language)) {
      return failedFuture(new IllegalArgumentException("Choose a target language."));
    }

    MessageTranslationBackend backend = backendRegistry.find(settings.backendId()).orElse(null);
    if (backend == null) {
      return failedFuture(
          new IllegalStateException("Translation backend is unavailable: " + settings.backendId()));
    }

    if (!tryAcquireSlot(settings)) {
      return failedFuture(new IllegalStateException("Too many translation requests are active."));
    }

    MessageTranslationRequest request =
        new MessageTranslationRequest(
            target,
            Instant.now(),
            "",
            "outbound-" + System.nanoTime(),
            body,
            settings.sourceLanguage(),
            language);
    long timeoutMs = Math.max(1L, settings.requestTimeoutMs());

    try {
      return CompletableFuture.supplyAsync(() -> backend.translate(request), translationExecutor)
          .thenCompose(stage -> stage == null ? failedFuture(nullStageException()) : stage)
          .orTimeout(timeoutMs, TimeUnit.MILLISECONDS)
          .whenComplete((ignored, error) -> releaseSlot());
    } catch (RuntimeException ex) {
      releaseSlot();
      return failedFuture(ex);
    }
  }

  private boolean tryAcquireSlot(IrcProperties.Client.Translation settings) {
    int limit = Math.max(1, settings.maxConcurrentRequests());
    while (true) {
      int current = inFlight.get();
      if (current >= limit) {
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

  private static String normalizeLanguage(String value) {
    return Objects.toString(value, "").trim().toLowerCase(java.util.Locale.ROOT);
  }

  private static IllegalStateException nullStageException() {
    return new IllegalStateException("Translation backend returned no result.");
  }

  private static <T> CompletableFuture<T> failedFuture(Throwable error) {
    return CompletableFuture.failedFuture(error);
  }
}
