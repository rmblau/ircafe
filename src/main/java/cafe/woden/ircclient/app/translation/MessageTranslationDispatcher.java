package cafe.woden.ircclient.app.translation;

import cafe.woden.ircclient.app.api.MessageTranslation;
import cafe.woden.ircclient.app.api.UiPort;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationLanguage;
import cafe.woden.ircclient.config.IrcProperties;
import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.config.execution.ExecutorConfig;
import cafe.woden.ircclient.model.TargetRef;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutorService;
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
  private final MessageTranslationDispatchPlanningService planningService;
  private final MessageTranslationExecutionService executionService;
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
    MessageTranslationSettingsSnapshot translation = currentSettings();
    if (MessageTranslationTargetRefAdapter.unavailableOrUiOnly(target)) {
      logManualSkip(automatic, "target is unavailable or UI-only");
      return false;
    }

    MessageTranslationDispatchPlanningService.PlanningResult planningResult =
        planningService.plan(
            new MessageTranslationDispatchPlanningService.PlanningInput(
                translation,
                MessageTranslationTargetRefAdapter.toTargetView(target),
                at,
                fromNick,
                messageId,
                text,
                targetLanguageOverride,
                automatic,
                () -> detectionLanguageCodes(translation)));
    if (!planningResult.accepted()) {
      logManualSkip(automatic, planningResult.skipReason());
      return false;
    }

    MessageTranslationDispatchPlanningService.TranslationPlan plan = planningResult.plan();
    if (!tryReserveSlot(plan.maxConcurrentRequests())) {
      logManualSkip(
          automatic,
          "translation concurrency limit reached (max={})",
          plan.maxConcurrentRequests());
      return false;
    }

    try {
      translationExecutor.execute(() -> runTranslation(plan));
      if (!automatic) {
        log.info(
            "[translation] scheduled manual request target={} msgid={} backend={} source={} target={}",
            target,
            plan.request().messageId(),
            plan.backend().backendId(),
            plan.request().sourceLanguage(),
            plan.request().targetLanguage());
      }
      return true;
    } catch (RuntimeException ex) {
      releaseSlot();
      logManualSkip(automatic, "translation executor rejected request: {}", ex.toString());
      return false;
    }
  }

  private void runTranslation(MessageTranslationDispatchPlanningService.TranslationPlan plan) {
    boolean completionRegistered = false;
    try {
      executionService
          .translate(
              new MessageTranslationExecutionService.ExecutionInput(
                  plan.backend(),
                  plan.request(),
                  plan.backendContext(),
                  plan.requestTimeoutMs(),
                  plan.suppressSameLanguageResult(),
                  plan.translateUnknownMessages(),
                  plan.detectionLanguageCodes()))
          .whenComplete(
              (result, error) -> {
                try {
                  if (error != null) {
                    log.warn(
                        "[translation] execution failed target={} msgid={} backend={} error={}",
                        plan.request().target(),
                        plan.request().messageId(),
                        plan.backend().backendId(),
                        error.toString());
                    return;
                  }
                  applyTranslationResult(result);
                } finally {
                  releaseSlot();
                }
              });
      completionRegistered = true;
    } catch (Throwable ex) {
      log.warn(
          "[translation] backend threw before registering completion target={} msgid={} backend={} error={}",
          plan.request().target(),
          plan.request().messageId(),
          plan.backend().backendId(),
          ex.toString());
    } finally {
      if (!completionRegistered) {
        releaseSlot();
      }
    }
  }

  private void applyTranslationResult(
      MessageTranslationExecutionService.ExecutionResult executionResult) {
    if (executionResult == null) {
      log.warn("[translation] execution returned no result");
      return;
    }
    MessageTranslationRenderResult renderResult = executionResult.renderResult();
    if (executionResult.failed()) {
      log.warn(
          "[translation] backend failed target={} msgid={} error={}",
          executionResult.request().target(),
          executionResult.request().messageId(),
          executionResult.error().toString());
      return;
    }
    if (executionResult.skipped()) {
      return;
    }
    if (renderResult == null) {
      log.warn(
          "[translation] execution produced no render result target={} msgid={}",
          executionResult.request().target(),
          executionResult.request().messageId());
      return;
    }
    UiPort ui = uiProvider.getIfAvailable();
    if (ui == null) {
      log.warn(
          "[translation] UI port unavailable for translation result target={} msgid={} backend={}",
          renderResult.target(),
          renderResult.targetMessageId(),
          renderResult.provider());
      return;
    }
    boolean applied =
        ui.applyMessageTranslation(
            MessageTranslationTargetRefAdapter.toTargetRef(renderResult.target()),
            renderResult.at(),
            toMessageTranslation(renderResult));
    if (!applied) {
      log.warn(
          "[translation] UI did not apply translation target={} msgid={} backend={}",
          renderResult.target(),
          renderResult.targetMessageId(),
          renderResult.provider());
    }
  }

  private static MessageTranslation toMessageTranslation(MessageTranslationRenderResult result) {
    return new MessageTranslation(
        result.targetMessageId(),
        result.translatedText(),
        result.sourceLanguage(),
        result.targetLanguage(),
        result.provider());
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

  private MessageTranslationSettingsSnapshot currentSettings() {
    if (settingsBus != null) {
      return settingsBus.snapshot();
    }
    IrcProperties.Client.Translation translation =
        ircProperties == null || ircProperties.client() == null
            ? null
            : ircProperties.client().translation();
    return MessageTranslationSettingsBus.snapshot(translation);
  }

  private List<String> detectionLanguageCodes(MessageTranslationSettingsSnapshot translation) {
    return MessageTranslationLanguageCatalogSupport.availableTargets(translation, installedPlugins)
        .stream()
        .map(MessageTranslationLanguage::code)
        .toList();
  }

  private static void logManualSkip(boolean automatic, String message, Object... args) {
    if (!automatic) {
      log.warn("[translation] manual request skipped: " + message, args);
    }
  }
}
