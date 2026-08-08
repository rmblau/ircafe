package cafe.woden.ircclient.app.translation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cafe.woden.ircclient.app.translation.spi.MessageTranslationBackendContext;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationBackendProvider;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationRequest;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationResult;
import cafe.woden.ircclient.config.IrcProperties;
import cafe.woden.ircclient.model.TargetRef;
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

class OutboundMessageTranslationServiceTest {

  private static final TargetRef TARGET = new TargetRef("libera", "#ircafe");
  private ExecutorService executor;

  @BeforeEach
  void setUp() {
    executor =
        Executors.newSingleThreadExecutor(
            task -> {
              Thread thread = new Thread(task);
              thread.setName("outbound-translation-test");
              return thread;
            });
  }

  @AfterEach
  void tearDown() {
    executor.shutdownNow();
  }

  @Test
  void translatesDraftOnTranslationExecutor() throws Exception {
    AtomicReference<MessageTranslationRequest> requestRef = new AtomicReference<>();
    AtomicReference<String> threadName = new AtomicReference<>();
    CapturingBackend backend =
        new CapturingBackend(
            request -> {
              requestRef.set(request);
              threadName.set(Thread.currentThread().getName());
              return CompletableFuture.completedFuture(
                  new MessageTranslationResult("hola mundo", "en", "es", "test"));
            });
    OutboundMessageTranslationService service = service(props(true, "test", "es"), backend);

    MessageTranslationResult result =
        service
            .translateDraft(TARGET, "hello world", "es")
            .toCompletableFuture()
            .get(1, TimeUnit.SECONDS);

    assertEquals("hola mundo", result.translatedText());
    assertEquals("outbound-translation-test", threadName.get());
    assertEquals("libera", requestRef.get().target().serverId());
    assertEquals("#ircafe", requestRef.get().target().target());
    assertEquals("hello world", requestRef.get().text());
    assertEquals("es", requestRef.get().targetLanguage());
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
    OutboundMessageTranslationService service =
        service(
            props(true, "test", "es", "https://translation.example/api", "secret-token", 2_500),
            backend);

    service
        .translateDraft(TARGET, "hello world", "es")
        .toCompletableFuture()
        .get(1, TimeUnit.SECONDS);

    assertEquals("https://translation.example/api", contextRef.get().endpoint());
    assertEquals("secret-token", contextRef.get().apiKey());
    assertEquals(2_500, contextRef.get().requestTimeoutMs());
  }

  @Test
  void rootFacadeRejectsUiOnlyTargetBeforeFeatureDelegate() {
    OutboundMessageTranslationService service =
        service(
            props(true, "test", "es"),
            new CapturingBackend(request -> CompletableFuture.completedFuture(null)));

    assertThrows(
        ExecutionException.class,
        () ->
            service
                .translateDraft(TargetRef.notifications("libera"), "hello", "es")
                .toCompletableFuture()
                .get());
  }

  @Test
  void failsWhenTranslationIsDisabled() {
    OutboundMessageTranslationService service =
        service(
            props(false, "", ""),
            new CapturingBackend(request -> CompletableFuture.completedFuture(null)));

    assertThrows(
        ExecutionException.class,
        () -> service.translateDraft(TARGET, "hello", "es").toCompletableFuture().get());
  }

  private OutboundMessageTranslationService service(
      IrcProperties props, MessageTranslationBackendProvider backend) {
    return new OutboundMessageTranslationService(
        new MessageTranslationSettingsBus(props),
        new MessageTranslationBackendRegistry(List.of(backend)),
        executor);
  }

  private static IrcProperties props(boolean enabled, String backend, String targetLanguage) {
    return props(enabled, backend, targetLanguage, "", "", 10_000);
  }

  private static IrcProperties props(
      boolean enabled,
      String backend,
      String targetLanguage,
      String endpoint,
      String apiKey,
      long requestTimeoutMs) {
    return new IrcProperties(
        new IrcProperties.Client(
            "IRCafe",
            null,
            null,
            null,
            null,
            new IrcProperties.Client.Translation(
                enabled,
                IrcProperties.Client.Translation.Mode.AUTO,
                backend,
                endpoint,
                apiKey,
                "auto",
                targetLanguage,
                true,
                true,
                List.of(),
                null,
                requestTimeoutMs,
                4_000,
                2)),
        List.of());
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
