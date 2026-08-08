package cafe.woden.ircclient.app.translation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cafe.woden.ircclient.app.translation.spi.MessageTranslationBackendContext;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationBackendProvider;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationRequest;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationResult;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationTargetView;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OutboundDraftTranslationServiceTest {

  private static final MessageTranslationTargetView TARGET =
      new MessageTranslationTargetView("libera", "#ircafe");

  private ExecutorService executor;

  @BeforeEach
  void setUp() {
    executor =
        Executors.newSingleThreadExecutor(
            task -> {
              Thread thread = new Thread(task);
              thread.setName("outbound-feature-translation-test");
              return thread;
            });
  }

  @AfterEach
  void tearDown() {
    executor.shutdownNow();
  }

  @Test
  void translatesDraftOnProvidedExecutor() throws Exception {
    AtomicReference<MessageTranslationRequest> requestRef = new AtomicReference<>();
    AtomicReference<String> threadName = new AtomicReference<>();
    CapturingBackend backend =
        new CapturingBackend(
            request -> {
              requestRef.set(request);
              threadName.set(Thread.currentThread().getName());
              return CompletableFuture.completedFuture(
                  new MessageTranslationResult("bonjour", "en", "fr", "test"));
            });
    OutboundDraftTranslationService service = service(settings(true, "test", "fr"), backend);

    MessageTranslationResult result =
        service.translateDraft(TARGET, "hello", "fr").toCompletableFuture().get(1, TimeUnit.SECONDS);

    assertEquals("bonjour", result.translatedText());
    assertEquals("outbound-feature-translation-test", threadName.get());
    assertEquals(TARGET, requestRef.get().target());
    assertEquals("hello", requestRef.get().text());
    assertEquals("fr", requestRef.get().targetLanguage());
  }

  @Test
  void passesRuntimeContextToBackend() throws Exception {
    AtomicReference<MessageTranslationBackendContext> contextRef = new AtomicReference<>();
    CapturingBackend backend =
        new CapturingBackend(
            request ->
                CompletableFuture.completedFuture(
                    new MessageTranslationResult("hola mundo", "en", "es", "test"))) {
          @Override
          public CompletionStage<MessageTranslationResult> translate(
              MessageTranslationRequest request, MessageTranslationBackendContext context) {
            contextRef.set(context);
            return super.translate(request);
          }
        };
    OutboundDraftTranslationService service =
        service(
            settings(
                true,
                "test",
                "es",
                "https://translation.example/api",
                "secret-token",
                2_500),
            backend);

    service.translateDraft(TARGET, "hello world", "es").toCompletableFuture().get(1, TimeUnit.SECONDS);

    assertEquals("https://translation.example/api", contextRef.get().endpoint());
    assertEquals("secret-token", contextRef.get().apiKey());
    assertEquals(2_500, contextRef.get().requestTimeoutMs());
  }

  @Test
  void failsWhenTranslationIsDisabled() {
    OutboundDraftTranslationService service =
        service(
            settings(false, "", ""),
            new CapturingBackend(request -> CompletableFuture.completedFuture(null)));

    assertThrows(
        ExecutionException.class,
        () -> service.translateDraft(TARGET, "hello", "es").toCompletableFuture().get());
  }

  private OutboundDraftTranslationService service(
      MessageTranslationSettingsSnapshot settings, MessageTranslationBackendProvider backend) {
    return new OutboundDraftTranslationService(
        () -> settings, new MessageTranslationBackendRegistry(List.of(backend)), executor);
  }

  private static MessageTranslationSettingsSnapshot settings(
      boolean enabled, String backend, String targetLanguage) {
    return settings(enabled, backend, targetLanguage, "", "", 10_000);
  }

  private static MessageTranslationSettingsSnapshot settings(
      boolean enabled,
      String backend,
      String targetLanguage,
      String endpoint,
      String apiKey,
      long requestTimeoutMs) {
    return new MessageTranslationSettingsSnapshot(
        enabled,
        MessageTranslationSettingsSnapshot.Mode.AUTO,
        backend,
        endpoint,
        apiKey,
        "auto",
        targetLanguage,
        true,
        true,
        List.of(),
        requestTimeoutMs,
        4_000,
        2);
  }

  private static class CapturingBackend implements MessageTranslationBackendProvider {
    private final java.util.function.Function<
            MessageTranslationRequest, CompletionStage<MessageTranslationResult>>
        handler;

    private CapturingBackend(
        java.util.function.Function<
                MessageTranslationRequest, CompletionStage<MessageTranslationResult>>
            handler) {
      this.handler = handler;
    }

    @Override
    public String backendId() {
      return "test";
    }

    @Override
    public CompletionStage<MessageTranslationResult> translate(MessageTranslationRequest request) {
      return handler.apply(request);
    }
  }
}
