package cafe.woden.ircclient.app.translation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import cafe.woden.ircclient.app.api.MessageTranslation;
import cafe.woden.ircclient.app.api.UiPort;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationBackendProvider;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationLanguageProvider;
import cafe.woden.ircclient.config.IrcProperties;
import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.model.TargetRef;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

class MessageTranslationDispatcherTest {

  private static final TargetRef TARGET = new TargetRef("libera", "#ircafe");
  private static final Instant AT = Instant.parse("2026-06-01T12:00:00Z");

  private ExecutorService executor;

  @BeforeEach
  void setUp() {
    executor =
        Executors.newSingleThreadExecutor(
            task -> {
              Thread thread = new Thread(task);
              thread.setName("translation-dispatcher-test");
              return thread;
            });
  }

  @AfterEach
  void tearDown() {
    executor.shutdownNow();
  }

  @Test
  void skipsWhenTranslationDisabled() {
    UiPort ui = mock(UiPort.class);
    CapturingBackend backend =
        new CapturingBackend(
            "deepl",
            request ->
                CompletableFuture.completedFuture(
                    new MessageTranslationResult("hola", "", "", "")));
    MessageTranslationDispatcher dispatcher =
        dispatcher(props(false, "", "auto", "", 4_000, 2, 10_000), ui, backend);

    assertFalse(
        dispatcher.requestIncomingMessageTranslation(TARGET, AT, "alice", "msg-1", "hello"));
    assertEquals(0, backend.requests.size());
    verifyNoInteractions(ui);
  }

  @Test
  void skipsWhenConfiguredBackendIsMissing() {
    UiPort ui = mock(UiPort.class);
    MessageTranslationDispatcher dispatcher =
        dispatcher(props(true, "deepl", "auto", "es", 4_000, 2, 10_000), ui);

    assertFalse(
        dispatcher.requestIncomingMessageTranslation(TARGET, AT, "alice", "msg-1", "hello"));
    verifyNoInteractions(ui);
  }

  @Test
  void skipsOverSizedMessagesBeforeCallingBackend() {
    UiPort ui = mock(UiPort.class);
    CapturingBackend backend =
        new CapturingBackend(
            "deepl",
            request ->
                CompletableFuture.completedFuture(
                    new MessageTranslationResult("hola", "", "", "")));
    MessageTranslationDispatcher dispatcher =
        dispatcher(props(true, "deepl", "auto", "es", 4, 2, 10_000), ui, backend);

    assertFalse(
        dispatcher.requestIncomingMessageTranslation(TARGET, AT, "alice", "msg-1", "hello"));
    assertEquals(0, backend.requests.size());
    verifyNoInteractions(ui);
  }

  @Test
  void appliesCompletedTranslationToOriginalMessageId() {
    UiPort ui = mock(UiPort.class);
    CapturingBackend backend =
        new CapturingBackend(
            "deepl",
            request ->
                CompletableFuture.completedFuture(
                    new MessageTranslationResult("hola a todos", "en", "", "")));
    MessageTranslationDispatcher dispatcher =
        dispatcher(props(true, "deepl", "auto", "es", 4_000, 2, 10_000), ui, backend);

    assertTrue(
        dispatcher.requestIncomingMessageTranslation(
            TARGET, AT, "alice", "msg-1", "hello everyone"));

    MessageTranslation expected =
        new MessageTranslation("msg-1", "hola a todos", "en", "es", "deepl");
    verify(ui, timeout(1_000)).applyMessageTranslation(eq(TARGET), eq(AT), eq(expected));
    assertEquals(1, backend.requests.size());
    assertEquals("alice", backend.requests.getFirst().fromNick());
    assertEquals("hello everyone", backend.requests.getFirst().text());
  }

  @Test
  void manualModeSkipsIncomingAutomaticTranslations() {
    UiPort ui = mock(UiPort.class);
    CapturingBackend backend =
        new CapturingBackend(
            "deepl",
            request ->
                CompletableFuture.completedFuture(
                    new MessageTranslationResult("hola", "en", "es", "deepl")));
    MessageTranslationDispatcher dispatcher =
        dispatcher(
            props(
                IrcProperties.Client.Translation.Mode.MANUAL,
                true,
                "deepl",
                "auto",
                "es",
                4_000,
                2,
                10_000),
            ui,
            backend);

    assertFalse(
        dispatcher.requestIncomingMessageTranslation(TARGET, AT, "alice", "msg-1", "hello"));
    assertEquals(0, backend.requests.size());
    verifyNoInteractions(ui);
  }

  @Test
  void manualTranslationUsesSelectedTargetLanguage() {
    UiPort ui = mock(UiPort.class);
    CapturingBackend backend =
        new CapturingBackend(
            "deepl",
            request ->
                CompletableFuture.completedFuture(
                    new MessageTranslationResult("bonjour", "en", "fr", "deepl")));
    MessageTranslationDispatcher dispatcher =
        dispatcher(
            props(
                IrcProperties.Client.Translation.Mode.MANUAL,
                true,
                "deepl",
                "auto",
                "es",
                4_000,
                2,
                10_000),
            ui,
            backend);

    assertTrue(
        dispatcher.requestManualMessageTranslation(TARGET, AT, "alice", "msg-1", "hello", "fr"));

    MessageTranslation expected = new MessageTranslation("msg-1", "bonjour", "en", "fr", "deepl");
    verify(ui, timeout(1_000)).applyMessageTranslation(eq(TARGET), eq(AT), eq(expected));
    assertEquals("fr", backend.requests.getFirst().targetLanguage());
  }

  @Test
  void manualTranslationIgnoresUnknownLanguageAutoSkipSetting() {
    UiPort ui = mock(UiPort.class);
    CapturingBackend backend =
        new CapturingBackend(
            "deepl",
            request ->
                CompletableFuture.completedFuture(
                    new MessageTranslationResult("hello", "es", "en", "deepl")));
    MessageTranslationDispatcher dispatcher =
        dispatcherWithDetector(
            props(
                IrcProperties.Client.Translation.Mode.MANUAL,
                true,
                "deepl",
                "auto",
                "es",
                4_000,
                2,
                10_000,
                false),
            ui,
            text -> Optional.empty(),
            backend);

    assertTrue(
        dispatcher.requestManualMessageTranslation(
            TARGET, AT, "alice", "msg-1", "hola a todos", "en"));

    MessageTranslation expected = new MessageTranslation("msg-1", "hello", "es", "en", "deepl");
    verify(ui, timeout(1_000)).applyMessageTranslation(eq(TARGET), eq(AT), eq(expected));
    assertEquals(1, backend.requests.size());
    assertEquals("en", backend.requests.getFirst().targetLanguage());
  }

  @Test
  void autoModeSuppressesRenderedTranslationWhenDetectedSourceMatchesTarget() {
    UiPort ui = mock(UiPort.class);
    CapturingBackend backend =
        new CapturingBackend(
            "deepl",
            request ->
                CompletableFuture.completedFuture(
                    new MessageTranslationResult("hola", "es", "es", "deepl")));
    MessageTranslationDispatcher dispatcher =
        dispatcher(props(true, "deepl", "auto", "es", 4_000, 2, 10_000), ui, backend);

    assertTrue(dispatcher.requestIncomingMessageTranslation(TARGET, AT, "alice", "msg-1", "hola"));

    verify(ui, after(250).never()).applyMessageTranslation(any(), any(), any());
    assertEquals(1, backend.requests.size());
  }

  @Test
  void autoModeSkipsBackendWhenLocalDetectorMatchesTargetLanguage() {
    UiPort ui = mock(UiPort.class);
    CapturingBackend backend =
        new CapturingBackend(
            "deepl",
            request ->
                CompletableFuture.completedFuture(
                    new MessageTranslationResult("hola", "es", "es", "deepl")));
    MessageTranslationDispatcher dispatcher =
        dispatcherWithDetector(
            props(true, "deepl", "auto", "es", 4_000, 2, 10_000),
            ui,
            text -> Optional.of("es"),
            backend);

    assertTrue(
        dispatcher.requestIncomingMessageTranslation(
            TARGET, AT, "alice", "msg-1", "hola, gracias por tu ayuda"));

    verify(ui, after(250).never()).applyMessageTranslation(any(), any(), any());
    assertEquals(0, backend.requests.size());
  }

  @Test
  void autoModeUsesLocalDetectorSourceWhenItDiffersFromTargetLanguage() {
    UiPort ui = mock(UiPort.class);
    CapturingBackend backend =
        new CapturingBackend(
            "deepl",
            request ->
                CompletableFuture.completedFuture(
                    new MessageTranslationResult("hola a todos", "", "", "")));
    MessageTranslationDispatcher dispatcher =
        dispatcherWithDetector(
            props(true, "deepl", "auto", "es", 4_000, 2, 10_000),
            ui,
            text -> Optional.of("en"),
            backend);

    assertTrue(
        dispatcher.requestIncomingMessageTranslation(
            TARGET, AT, "alice", "msg-1", "hello everyone"));

    MessageTranslation expected =
        new MessageTranslation("msg-1", "hola a todos", "en", "es", "deepl");
    verify(ui, timeout(1_000)).applyMessageTranslation(eq(TARGET), eq(AT), eq(expected));
    assertEquals("en", backend.requests.getFirst().sourceLanguage());
  }

  @Test
  void autoModeRunsLanguageDetectionOnTranslationExecutor() throws Exception {
    UiPort ui = mock(UiPort.class);
    CountDownLatch detected = new CountDownLatch(1);
    AtomicReference<String> detectorThreadName = new AtomicReference<>("");
    CapturingBackend backend =
        new CapturingBackend(
            "deepl",
            request ->
                CompletableFuture.completedFuture(
                    new MessageTranslationResult("hola", "es", "es", "deepl")));
    MessageTranslationDispatcher dispatcher =
        dispatcherWithDetector(
            props(true, "deepl", "auto", "es", 4_000, 2, 10_000),
            ui,
            text -> {
              detectorThreadName.set(Thread.currentThread().getName());
              detected.countDown();
              return Optional.of("es");
            },
            backend);

    assertTrue(
        dispatcher.requestIncomingMessageTranslation(
            TARGET, AT, "alice", "msg-1", "hola, gracias por tu ayuda"));

    assertTrue(detected.await(1, TimeUnit.SECONDS));
    assertTrue(detectorThreadName.get().startsWith("translation-dispatcher-test"));
    verify(ui, after(100).never()).applyMessageTranslation(any(), any(), any());
    assertEquals(0, backend.requests.size());
  }

  @Test
  void autoModeSkipsUnknownLanguageWhenConfigured() {
    UiPort ui = mock(UiPort.class);
    CapturingBackend backend =
        new CapturingBackend(
            "deepl",
            request ->
                CompletableFuture.completedFuture(
                    new MessageTranslationResult("hola", "", "", "deepl")));
    MessageTranslationDispatcher dispatcher =
        dispatcherWithDetector(
            props(true, "deepl", "auto", "es", 4_000, 2, 10_000, false),
            ui,
            text -> Optional.empty(),
            backend);

    assertTrue(dispatcher.requestIncomingMessageTranslation(TARGET, AT, "alice", "msg-1", "ok"));

    verify(ui, after(250).never()).applyMessageTranslation(any(), any(), any());
    assertEquals(0, backend.requests.size());
  }

  @Test
  void autoModePassesConfiguredDetectionLanguagesToDetector() {
    UiPort ui = mock(UiPort.class);
    CapturingDetector detector = new CapturingDetector(Optional.of("en"));
    CapturingBackend backend =
        new CapturingBackend(
            "deepl",
            request ->
                CompletableFuture.completedFuture(
                    new MessageTranslationResult("hola", "en", "es", "deepl")));
    MessageTranslationDispatcher dispatcher =
        dispatcherWithDetector(
            props(
                IrcProperties.Client.Translation.Mode.AUTO,
                true,
                "deepl",
                "auto",
                "es",
                4_000,
                2,
                10_000,
                true,
                false,
                List.of("en", "es")),
            ui,
            detector,
            backend);

    assertTrue(
        dispatcher.requestIncomingMessageTranslation(
            TARGET, AT, "alice", "msg-1", "hello everyone"));

    verify(ui, timeout(1_000)).applyMessageTranslation(eq(TARGET), eq(AT), any());
    assertEquals(List.of("en", "es"), detector.lastLanguageCodes);
    assertEquals("en", backend.requests.getFirst().sourceLanguage());
  }

  @Test
  void autoModeIncludesPluginLanguagesForDetection() {
    UiPort ui = mock(UiPort.class);
    CapturingDetector detector = new CapturingDetector(Optional.of("en"));
    CapturingBackend backend =
        new CapturingBackend(
            "deepl",
            request ->
                CompletableFuture.completedFuture(
                    new MessageTranslationResult("hola", "en", "es", "deepl")));
    MessageTranslationDispatcher dispatcher =
        dispatcherWithDetector(
            props(true, "deepl", "auto", "es", 4_000, 2, 10_000), ui, detector, backend);
    dispatcher.setInstalledPlugins(
        new RecordingInstalledPluginsPort(
            List.of(() -> List.of(new MessageTranslationLanguage("tlh", "Klingon")))));

    assertTrue(
        dispatcher.requestIncomingMessageTranslation(
            TARGET, AT, "alice", "msg-1", "hello everyone"));

    verify(ui, timeout(1_000)).applyMessageTranslation(eq(TARGET), eq(AT), any());
    assertTrue(detector.lastLanguageCodes.contains("tlh"), detector.lastLanguageCodes.toString());
  }

  @Test
  void rejectsRequestsOverConfiguredConcurrencyLimit() {
    UiPort ui = mock(UiPort.class);
    CompletableFuture<MessageTranslationResult> pending = new CompletableFuture<>();
    CapturingBackend backend = new CapturingBackend("deepl", request -> pending);
    MessageTranslationDispatcher dispatcher =
        dispatcher(props(true, "deepl", "auto", "es", 4_000, 1, 10_000), ui, backend);

    assertTrue(dispatcher.requestIncomingMessageTranslation(TARGET, AT, "alice", "msg-1", "hello"));
    assertFalse(dispatcher.requestIncomingMessageTranslation(TARGET, AT, "bob", "msg-2", "hola"));
  }

  private MessageTranslationDispatcher dispatcher(
      IrcProperties props, UiPort ui, MessageTranslationBackendProvider... backends) {
    return dispatcherWithDetector(props, ui, text -> Optional.empty(), backends);
  }

  private MessageTranslationDispatcher dispatcherWithDetector(
      IrcProperties props,
      UiPort ui,
      MessageLanguageDetector languageDetector,
      MessageTranslationBackendProvider... backends) {
    return new MessageTranslationDispatcher(
        props,
        new MessageTranslationSettingsBus(props),
        new MessageTranslationBackendRegistry(List.of(backends)),
        languageDetector,
        fixedProvider(ui),
        executor);
  }

  private static ObjectProvider<UiPort> fixedProvider(UiPort ui) {
    return new ObjectProvider<>() {
      @Override
      public UiPort getObject(Object... args) {
        return ui;
      }

      @Override
      public UiPort getIfAvailable() {
        return ui;
      }

      @Override
      public UiPort getIfUnique() {
        return ui;
      }

      @Override
      public UiPort getObject() {
        return ui;
      }
    };
  }

  private static IrcProperties props(
      boolean enabled,
      String backend,
      String sourceLanguage,
      String targetLanguage,
      int maxRequestChars,
      int maxConcurrentRequests,
      long requestTimeoutMs) {
    return props(
        IrcProperties.Client.Translation.Mode.AUTO,
        enabled,
        backend,
        sourceLanguage,
        targetLanguage,
        maxRequestChars,
        maxConcurrentRequests,
        requestTimeoutMs);
  }

  private static IrcProperties props(
      boolean enabled,
      String backend,
      String sourceLanguage,
      String targetLanguage,
      int maxRequestChars,
      int maxConcurrentRequests,
      long requestTimeoutMs,
      boolean translateUnknownMessages) {
    return props(
        IrcProperties.Client.Translation.Mode.AUTO,
        enabled,
        backend,
        sourceLanguage,
        targetLanguage,
        maxRequestChars,
        maxConcurrentRequests,
        requestTimeoutMs,
        translateUnknownMessages);
  }

  private static IrcProperties props(
      IrcProperties.Client.Translation.Mode mode,
      boolean enabled,
      String backend,
      String sourceLanguage,
      String targetLanguage,
      int maxRequestChars,
      int maxConcurrentRequests,
      long requestTimeoutMs) {
    return props(
        mode,
        enabled,
        backend,
        sourceLanguage,
        targetLanguage,
        maxRequestChars,
        maxConcurrentRequests,
        requestTimeoutMs,
        true);
  }

  private static IrcProperties props(
      IrcProperties.Client.Translation.Mode mode,
      boolean enabled,
      String backend,
      String sourceLanguage,
      String targetLanguage,
      int maxRequestChars,
      int maxConcurrentRequests,
      long requestTimeoutMs,
      boolean translateUnknownMessages) {
    return new IrcProperties(
        new IrcProperties.Client(
            "IRCafe",
            null,
            null,
            null,
            null,
            new IrcProperties.Client.Translation(
                enabled,
                mode,
                backend,
                "",
                "test-key",
                sourceLanguage,
                targetLanguage,
                translateUnknownMessages,
                true,
                List.of(),
                null,
                requestTimeoutMs,
                maxRequestChars,
                maxConcurrentRequests)),
        List.of());
  }

  private static IrcProperties props(
      IrcProperties.Client.Translation.Mode mode,
      boolean enabled,
      String backend,
      String sourceLanguage,
      String targetLanguage,
      int maxRequestChars,
      int maxConcurrentRequests,
      long requestTimeoutMs,
      boolean translateUnknownMessages,
      boolean detectAllLanguages,
      List<String> detectionLanguages) {
    return new IrcProperties(
        new IrcProperties.Client(
            "IRCafe",
            null,
            null,
            null,
            null,
            new IrcProperties.Client.Translation(
                enabled,
                mode,
                backend,
                "",
                "test-key",
                sourceLanguage,
                targetLanguage,
                translateUnknownMessages,
                detectAllLanguages,
                detectionLanguages,
                null,
                requestTimeoutMs,
                maxRequestChars,
                maxConcurrentRequests)),
        List.of());
  }

  private static final class CapturingBackend implements MessageTranslationBackendProvider {
    private final String backendId;
    private final Function<MessageTranslationRequest, CompletionStage<MessageTranslationResult>>
        handler;
    private final List<MessageTranslationRequest> requests = new CopyOnWriteArrayList<>();

    private CapturingBackend(
        String backendId,
        Function<MessageTranslationRequest, CompletionStage<MessageTranslationResult>> handler) {
      this.backendId = backendId;
      this.handler = handler;
    }

    @Override
    public String backendId() {
      return backendId;
    }

    @Override
    public CompletionStage<MessageTranslationResult> translate(MessageTranslationRequest request) {
      requests.add(request);
      return handler.apply(request);
    }
  }

  private static final class CapturingDetector implements MessageLanguageDetector {
    private final Optional<String> result;
    private List<String> lastLanguageCodes = List.of();

    private CapturingDetector(Optional<String> result) {
      this.result = result;
    }

    @Override
    public Optional<String> detectLanguageCode(String text) {
      return result;
    }

    @Override
    public Optional<String> detectLanguageCode(String text, Collection<String> languageCodes) {
      lastLanguageCodes = List.copyOf(languageCodes);
      return result;
    }
  }

  private record RecordingInstalledPluginsPort(List<MessageTranslationLanguageProvider> providers)
      implements InstalledPluginsPort {

    @Override
    public <T> List<T> loadInstalledServices(Class<T> serviceType, List<T> builtInServices) {
      ArrayList<T> services = new ArrayList<>(builtInServices);
      if (serviceType == MessageTranslationLanguageProvider.class) {
        for (MessageTranslationLanguageProvider provider : providers) {
          services.add(serviceType.cast(provider));
        }
      }
      return List.copyOf(services);
    }
  }
}
