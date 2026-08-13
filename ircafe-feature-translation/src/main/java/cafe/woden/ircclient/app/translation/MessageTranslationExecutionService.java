package cafe.woden.ircclient.app.translation;

import cafe.woden.ircclient.app.translation.spi.MessageTranslationBackendContext;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationBackendProvider;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationRequest;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationResult;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import org.jmolecules.architecture.layered.ApplicationLayer;
import org.springframework.stereotype.Component;

/**
 * Runs a prepared translation request through backend preflight, backend execution, and result
 * filtering.
 */
@Component
@ApplicationLayer
public final class MessageTranslationExecutionService {
  private final MessageTranslationPreflightService preflightService;

  public MessageTranslationExecutionService(MessageTranslationPreflightService preflightService) {
    this.preflightService = Objects.requireNonNull(preflightService, "preflightService");
  }

  public CompletionStage<ExecutionResult> translate(ExecutionInput input) {
    Objects.requireNonNull(input, "input");
    MessageTranslationPreflightService.PreflightResult prepared =
        preflightService.prepareBackendRequest(
            new MessageTranslationPreflightService.AutomaticPreflightInput(
                input.request(),
                input.suppressSameLanguageResult(),
                input.translateUnknownMessages(),
                input.detectionLanguageCodes()));
    if (!prepared.accepted()) {
      return CompletableFuture.completedFuture(
          ExecutionResult.skipped(input.request(), prepared.skipReason()));
    }

    MessageTranslationRequest preparedRequest = prepared.request();
    CompletionStage<MessageTranslationResult> backendStage;
    try {
      backendStage = input.backend().translate(preparedRequest, input.backendContext());
    } catch (Throwable ex) {
      return CompletableFuture.completedFuture(ExecutionResult.failed(preparedRequest, ex));
    }
    if (backendStage == null) {
      return CompletableFuture.completedFuture(
          ExecutionResult.skipped(preparedRequest, "backend returned no completion stage"));
    }

    CompletableFuture<MessageTranslationResult> completion;
    try {
      completion = backendStage.toCompletableFuture();
    } catch (Throwable ex) {
      return CompletableFuture.completedFuture(ExecutionResult.failed(preparedRequest, ex));
    }

    return completion
        .orTimeout(Math.max(1L, input.timeoutMs()), TimeUnit.MILLISECONDS)
        .handle(
            (result, error) -> {
              if (error != null) {
                return ExecutionResult.failed(preparedRequest, error);
              }
              if (result == null || result.translatedText().isBlank()) {
                return ExecutionResult.skipped(preparedRequest, "backend returned an empty result");
              }
              if (preflightService.shouldSuppressTranslationResult(
                  preparedRequest, result, input.suppressSameLanguageResult())) {
                return ExecutionResult.skipped(
                    preparedRequest, "translation result did not require rendering");
              }
              return ExecutionResult.translated(
                  preparedRequest,
                  result,
                  MessageTranslationRenderResult.from(input.backend(), preparedRequest, result));
            });
  }

  public record ExecutionInput(
      MessageTranslationBackendProvider backend,
      MessageTranslationRequest request,
      MessageTranslationBackendContext backendContext,
      long timeoutMs,
      boolean suppressSameLanguageResult,
      boolean translateUnknownMessages,
      List<String> detectionLanguageCodes) {
    public ExecutionInput {
      Objects.requireNonNull(backend, "backend");
      Objects.requireNonNull(request, "request");
      Objects.requireNonNull(backendContext, "backendContext");
      detectionLanguageCodes =
          detectionLanguageCodes == null ? List.of() : List.copyOf(detectionLanguageCodes);
    }
  }

  public record ExecutionResult(
      Outcome outcome,
      MessageTranslationRequest request,
      MessageTranslationResult result,
      MessageTranslationRenderResult renderResult,
      String skipReason,
      Throwable error) {
    public ExecutionResult {
      Objects.requireNonNull(outcome, "outcome");
      Objects.requireNonNull(request, "request");
    }

    public static ExecutionResult translated(
        MessageTranslationRequest request,
        MessageTranslationResult result,
        MessageTranslationRenderResult renderResult) {
      return new ExecutionResult(
          Outcome.TRANSLATED,
          request,
          Objects.requireNonNull(result, "result"),
          Objects.requireNonNull(renderResult, "renderResult"),
          "",
          null);
    }

    public static ExecutionResult skipped(MessageTranslationRequest request, String reason) {
      return new ExecutionResult(
          Outcome.SKIPPED,
          request,
          null,
          null,
          MessageTranslationPreflightService.firstNonBlank(reason, "skipped"),
          null);
    }

    public static ExecutionResult failed(MessageTranslationRequest request, Throwable error) {
      return new ExecutionResult(
          Outcome.FAILED, request, null, null, "", Objects.requireNonNull(error, "error"));
    }

    public boolean translated() {
      return outcome == Outcome.TRANSLATED;
    }

    public boolean skipped() {
      return outcome == Outcome.SKIPPED;
    }

    public boolean failed() {
      return outcome == Outcome.FAILED;
    }
  }

  public enum Outcome {
    TRANSLATED,
    SKIPPED,
    FAILED
  }
}
