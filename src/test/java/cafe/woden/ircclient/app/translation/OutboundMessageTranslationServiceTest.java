package cafe.woden.ircclient.app.translation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cafe.woden.ircclient.app.translation.spi.MessageTranslationBackendProvider;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationRequest;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationResult;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationTargetView;
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
  private static final MessageTranslationTargetView TARGET_VIEW =
      new MessageTranslationTargetView("libera", "#ircafe");

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
    assertEquals(TARGET_VIEW, requestRef.get().target());
    assertEquals("hello world", requestRef.get().text());
    assertEquals("es", requestRef.get().targetLanguage());
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
                "",
                "",
                "auto",
                targetLanguage,
                true,
                true,
                List.of(),
                null,
                10_000,
                4_000,
                2)),
        List.of());
  }

  private static final class CapturingBackend implements MessageTranslationBackendProvider {
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
