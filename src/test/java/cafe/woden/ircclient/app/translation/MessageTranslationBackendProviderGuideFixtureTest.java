package cafe.woden.ircclient.app.translation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.app.translation.spi.MessageTranslationBackendProvider;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationResult;
import cafe.woden.ircclient.config.IrcProperties;
import cafe.woden.ircclient.config.api.RuntimeConfigPathPort;
import cafe.woden.ircclient.config.plugins.InstalledPluginServices;
import cafe.woden.ircclient.config.plugins.InstalledPluginServicesTestSupport;
import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.util.CompiledPluginJarSupport;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MessageTranslationBackendProviderGuideFixtureTest {

  private static final String GUIDE_PROVIDER_CLASS =
      "example.translation.ExampleTranslationBackendProvider";

  @TempDir Path tempDir;

  private final ExecutorService executor =
      Executors.newSingleThreadExecutor(
          task -> {
            Thread thread = new Thread(task);
            thread.setName("translation-guide-test");
            return thread;
          });

  @AfterEach
  void tearDown() {
    executor.shutdownNow();
  }

  @Test
  void documentedTranslationBackendLoadsAndHandlesOutboundDraftRequests() throws Exception {
    Path runtimeConfigDirectory = Files.createDirectories(tempDir.resolve("config-home/ircafe"));
    Path pluginDir = Files.createDirectories(runtimeConfigDirectory.resolve("plugins"));
    CompiledPluginJarSupport.writePluginJar(
        pluginDir.resolve("translation-backend-guide-example.jar"),
        GUIDE_PROVIDER_CLASS,
        guideProviderSource(),
        MessageTranslationBackendProvider.class.getName(),
        CompiledPluginJarSupport.compatibleManifest("translation-backend-guide-example", "1.0.0"));
    RuntimeConfigPathPort runtimeConfigPathPort =
        () -> runtimeConfigDirectory.resolve("ircafe.yml");

    InstalledPluginServices installedPlugins = new InstalledPluginServices(runtimeConfigPathPort);
    try {
      MessageTranslationBackendRegistry registry =
          new MessageTranslationBackendRegistry(
              MessageTranslationPluginProviders.translationBackends(List.of(), installedPlugins));
      OutboundMessageTranslationService service =
          new OutboundMessageTranslationService(
              new MessageTranslationSettingsBus(props(" GUIDE-ECHO ", "es")), registry, executor);

      assertTrue(installedPlugins.pluginProblems().isEmpty());
      assertTrue(registry.backendIds().contains("guide-echo"));

      MessageTranslationResult result =
          service
              .translateDraft(new TargetRef("libera", "#ircafe"), "hello plugin", "FR")
              .toCompletableFuture()
              .get(1, TimeUnit.SECONDS);

      assertEquals("[libera/#ircafe] fr: hello plugin", result.translatedText());
      assertEquals("auto", result.sourceLanguage());
      assertEquals("fr", result.targetLanguage());
      assertEquals(
          "https://translate.example/v1|guide-token|10000", result.provider());
    } finally {
      InstalledPluginServicesTestSupport.shutdown(installedPlugins);
    }
  }

  private static IrcProperties props(String backendId, String targetLanguage) {
    return new IrcProperties(
        new IrcProperties.Client(
            "IRCafe",
            null,
            null,
            null,
            null,
            new IrcProperties.Client.Translation(
                true,
                IrcProperties.Client.Translation.Mode.MANUAL,
                backendId,
                " https://translate.example/v1 ",
                " guide-token ",
                "auto",
                targetLanguage,
                true,
                true,
                List.of(),
                IrcProperties.Client.Translation.DisplayMode.BELOW_ORIGINAL,
                10_000,
                4_000,
                2)),
        List.of());
  }

  private static String guideProviderSource() {
    return """
        package example.translation;

        import cafe.woden.ircclient.app.translation.spi.MessageTranslationBackendContext;
        import cafe.woden.ircclient.app.translation.spi.MessageTranslationBackendProvider;
        import cafe.woden.ircclient.app.translation.spi.MessageTranslationRequest;
        import cafe.woden.ircclient.app.translation.spi.MessageTranslationResult;
        import java.util.concurrent.CompletableFuture;
        import java.util.concurrent.CompletionStage;

        public final class ExampleTranslationBackendProvider
            implements MessageTranslationBackendProvider {
          @Override
          public String backendId() {
            return " Guide-Echo ";
          }

          @Override
          public CompletionStage<MessageTranslationResult> translate(
              MessageTranslationRequest request, MessageTranslationBackendContext context) {
            String translated =
                "["
                    + request.target().serverId()
                    + "/"
                    + request.target().target()
                    + "] "
                    + request.targetLanguage()
                    + ": "
                    + request.text();
            String provider =
                context.endpoint()
                    + "|"
                    + context.apiKey()
                    + "|"
                    + context.requestTimeoutMs();
            return CompletableFuture.completedFuture(
                new MessageTranslationResult(
                    translated,
                    request.sourceLanguage(),
                    request.targetLanguage(),
                    provider));
          }
        }
        """;
  }
}
