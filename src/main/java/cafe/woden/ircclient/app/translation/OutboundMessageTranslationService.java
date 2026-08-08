package cafe.woden.ircclient.app.translation;

import cafe.woden.ircclient.app.translation.spi.MessageTranslationResult;
import cafe.woden.ircclient.config.execution.ExecutorConfig;
import cafe.woden.ircclient.model.TargetRef;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import org.jmolecules.architecture.layered.ApplicationLayer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/** Adapts root chat targets to the feature-owned outbound draft translation service. */
@Component
@ApplicationLayer
public class OutboundMessageTranslationService {

  private final OutboundDraftTranslationService delegate;

  public OutboundMessageTranslationService(
      MessageTranslationSettingsProvider settingsProvider,
      MessageTranslationBackendRegistry backendRegistry,
      @Qualifier(ExecutorConfig.TRANSLATION_EXECUTOR) ExecutorService translationExecutor) {
    this(
        new OutboundDraftTranslationService(
            settingsProvider, backendRegistry, translationExecutor));
  }

  OutboundMessageTranslationService(OutboundDraftTranslationService delegate) {
    this.delegate = Objects.requireNonNull(delegate, "delegate");
  }

  public CompletionStage<MessageTranslationResult> translateDraft(
      TargetRef target, String text, String targetLanguage) {
    if (MessageTranslationTargetRefAdapter.unavailableOrUiOnly(target)) {
      return failedFuture(new IllegalArgumentException("No chat target is active."));
    }
    return delegate.translateDraft(
        MessageTranslationTargetRefAdapter.toTargetView(target), text, targetLanguage);
  }

  private static <T> CompletableFuture<T> failedFuture(Throwable error) {
    return CompletableFuture.failedFuture(error);
  }
}
