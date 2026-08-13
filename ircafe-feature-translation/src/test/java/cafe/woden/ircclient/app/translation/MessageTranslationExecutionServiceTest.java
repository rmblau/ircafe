package cafe.woden.ircclient.app.translation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.app.translation.MessageTranslationExecutionService.ExecutionInput;
import cafe.woden.ircclient.app.translation.MessageTranslationExecutionService.ExecutionResult;
import cafe.woden.ircclient.app.translation.MessageTranslationExecutionService.Outcome;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationBackendContext;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationBackendProvider;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationRequest;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationResult;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationTargetView;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

class MessageTranslationExecutionServiceTest {

  private static final MessageTranslationTargetView TARGET =
      new MessageTranslationTargetView("libera", "#ircafe");
  private static final MessageTranslationRequest REQUEST =
      new MessageTranslationRequest(
          TARGET, Instant.parse("2026-06-01T12:00:00Z"), "alice", "msg-1", "hello", "auto", "es");
  private static final MessageTranslationBackendContext CONTEXT =
      new MessageTranslationBackendContext("", "", 2_000);

  @Test
  void translatesPreparedRequest() throws Exception {
    MessageTranslationResult translation = new MessageTranslationResult("hola", "en", "es", "test");
    CapturingBackend backend =
        new CapturingBackend(request -> CompletableFuture.completedFuture(translation));
    MessageTranslationExecutionService service = service(Optional.of("en"));

    ExecutionResult result =
        service
            .translate(input(backend, true, true))
            .toCompletableFuture()
            .get(1, TimeUnit.SECONDS);

    assertTrue(result.translated());
    assertEquals(Outcome.TRANSLATED, result.outcome());
    assertEquals(translation, result.result());
    assertEquals("en", result.request().sourceLanguage());
    assertEquals("en", backend.lastRequest.sourceLanguage());
    assertEquals(TARGET, result.renderResult().target());
    assertEquals("msg-1", result.renderResult().targetMessageId());
    assertEquals("hola", result.renderResult().translatedText());
    assertEquals("test", result.renderResult().provider());
  }

  @Test
  void skipsBackendWhenDetectedSourceMatchesTarget() throws Exception {
    CapturingBackend backend =
        new CapturingBackend(request -> CompletableFuture.completedFuture(null));
    MessageTranslationExecutionService service = service(Optional.of("es"));

    ExecutionResult result =
        service
            .translate(input(backend, true, false))
            .toCompletableFuture()
            .get(1, TimeUnit.SECONDS);

    assertTrue(result.skipped());
    assertEquals("detected source language matches target language", result.skipReason());
    assertEquals(0, backend.callCount);
  }

  @Test
  void manualExecutionDoesNotSuppressSameLanguageResult() throws Exception {
    MessageTranslationResult translation = new MessageTranslationResult("hola", "es", "es", "test");
    CapturingBackend backend =
        new CapturingBackend(request -> CompletableFuture.completedFuture(translation));
    MessageTranslationExecutionService service = service(Optional.empty());

    ExecutionResult result =
        service
            .translate(input(backend, false, true))
            .toCompletableFuture()
            .get(1, TimeUnit.SECONDS);

    assertTrue(result.translated());
    assertEquals(translation, result.result());
  }

  @Test
  void suppressesEmptyBackendResult() throws Exception {
    CapturingBackend backend =
        new CapturingBackend(
            request ->
                CompletableFuture.completedFuture(
                    new MessageTranslationResult(" ", "en", "es", "test")));
    MessageTranslationExecutionService service = service(Optional.of("en"));

    ExecutionResult result =
        service
            .translate(input(backend, true, true))
            .toCompletableFuture()
            .get(1, TimeUnit.SECONDS);

    assertTrue(result.skipped());
    assertEquals("backend returned an empty result", result.skipReason());
  }

  @Test
  void capturesSynchronousBackendFailure() throws Exception {
    IllegalStateException failure = new IllegalStateException("boom");
    CapturingBackend backend =
        new CapturingBackend(
            request -> {
              throw failure;
            });
    MessageTranslationExecutionService service = service(Optional.of("en"));

    ExecutionResult result =
        service
            .translate(input(backend, true, true))
            .toCompletableFuture()
            .get(1, TimeUnit.SECONDS);

    assertTrue(result.failed());
    assertSame(failure, result.error());
  }

  @Test
  void capturesBackendTimeout() throws Exception {
    CapturingBackend backend = new CapturingBackend(request -> new CompletableFuture<>());
    MessageTranslationExecutionService service = service(Optional.of("en"));

    ExecutionResult result =
        service
            .translate(input(backend, true, true, 1L))
            .toCompletableFuture()
            .get(1, TimeUnit.SECONDS);

    assertTrue(result.failed());
    assertFalse(result.error().toString().isBlank());
  }

  private static MessageTranslationExecutionService service(Optional<String> detectedLanguage) {
    return new MessageTranslationExecutionService(
        new MessageTranslationPreflightService(new FixedDetector(detectedLanguage)));
  }

  private static ExecutionInput input(
      MessageTranslationBackendProvider backend,
      boolean suppressSameLanguageResult,
      boolean translateUnknownMessages) {
    return input(backend, suppressSameLanguageResult, translateUnknownMessages, 2_000L);
  }

  private static ExecutionInput input(
      MessageTranslationBackendProvider backend,
      boolean suppressSameLanguageResult,
      boolean translateUnknownMessages,
      long timeoutMs) {
    return new ExecutionInput(
        backend,
        REQUEST,
        CONTEXT,
        timeoutMs,
        suppressSameLanguageResult,
        translateUnknownMessages,
        List.of("en", "es"));
  }

  private record FixedDetector(Optional<String> detectedLanguage)
      implements MessageLanguageDetector {

    @Override
    public Optional<String> detectLanguageCode(String text) {
      return detectedLanguage;
    }

    @Override
    public Optional<String> detectLanguageCode(String text, Collection<String> languageCodes) {
      return detectedLanguage;
    }
  }

  private static final class CapturingBackend implements MessageTranslationBackendProvider {
    private final Function<MessageTranslationRequest, CompletionStage<MessageTranslationResult>>
        handler;
    private MessageTranslationRequest lastRequest;
    private int callCount;

    private CapturingBackend(
        Function<MessageTranslationRequest, CompletionStage<MessageTranslationResult>> handler) {
      this.handler = handler;
    }

    @Override
    public String backendId() {
      return "test";
    }

    @Override
    public CompletionStage<MessageTranslationResult> translate(MessageTranslationRequest request) {
      throw new UnsupportedOperationException("context overload should be used");
    }

    @Override
    public CompletionStage<MessageTranslationResult> translate(
        MessageTranslationRequest request, MessageTranslationBackendContext context) {
      callCount++;
      lastRequest = request;
      return handler.apply(request);
    }
  }
}
