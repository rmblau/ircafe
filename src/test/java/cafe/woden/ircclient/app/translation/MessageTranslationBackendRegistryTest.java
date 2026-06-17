package cafe.woden.ircclient.app.translation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.app.translation.spi.MessageTranslationBackendProvider;
import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.config.api.RuntimeConfigPathPort;
import cafe.woden.ircclient.config.plugins.InstalledPluginServices;
import cafe.woden.ircclient.util.CompiledPluginJarSupport;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MessageTranslationBackendRegistryTest {

  private static final String PLUGIN_BACKEND_CLASS = "plugin.translation.PluginEchoBackend";
  private static final String SPI_BACKEND_CLASS = "plugin.translation.PluginSpiEchoBackend";

  @TempDir Path tempDir;

  @Test
  void resolvesBackendsByNormalizedId() {
    MessageTranslationBackendProvider backend = new StubBackend(" DeepL ");
    MessageTranslationBackendRegistry registry =
        new MessageTranslationBackendRegistry(List.of(backend));

    assertSame(backend, registry.find("deepl").orElseThrow());
    assertSame(backend, registry.find("  DEEPL ").orElseThrow());
    assertEquals(Set.of("deepl"), registry.backendIds());
  }

  @Test
  void returnsEmptyForUnknownBackend() {
    MessageTranslationBackendRegistry registry = new MessageTranslationBackendRegistry(List.of());

    assertEquals(Optional.empty(), registry.find("missing"));
  }

  @Test
  void includesBackendsLoadedThroughInstalledPluginPort() {
    MessageTranslationBackendProvider builtIn = new StubBackend("built-in");
    MessageTranslationBackendProvider plugin = new StubBackend("plugin-extra");
    MessageTranslationBackendRegistry registry =
        new MessageTranslationBackendRegistry(
            List.of(builtIn), new RecordingInstalledPluginsPort(List.of(plugin)));

    assertSame(builtIn, registry.find("built-in").orElseThrow());
    assertSame(plugin, registry.find(" PLUGIN-EXTRA ").orElseThrow());
    assertEquals(Set.of("built-in", "plugin-extra"), registry.backendIds());
  }

  @Test
  void loadsServiceLoaderTranslationBackendsFromInstalledPlugins() throws Exception {
    Path runtimeConfigDirectory = Files.createDirectories(tempDir.resolve("config-home/ircafe"));
    Path pluginDir = Files.createDirectories(runtimeConfigDirectory.resolve("plugins"));
    CompiledPluginJarSupport.writePluginJar(
        pluginDir.resolve("example-translation-backend.jar"),
        PLUGIN_BACKEND_CLASS,
        pluginBackendSource(),
        MessageTranslationBackendProvider.class.getName(),
        CompiledPluginJarSupport.compatibleManifest("example-translation-backend", "1.0.0"));
    RuntimeConfigPathPort runtimeConfigPathPort =
        () -> runtimeConfigDirectory.resolve("ircafe.yml");

    InstalledPluginServices installedPlugins = new InstalledPluginServices(runtimeConfigPathPort);
    MessageTranslationBackendRegistry registry =
        new MessageTranslationBackendRegistry(List.of(), installedPlugins);

    assertTrue(installedPlugins.pluginProblems().isEmpty());
    MessageTranslationBackendProvider backend = registry.find("PLUGIN-ECHO").orElseThrow();
    MessageTranslationResult result = backend.translate(null).toCompletableFuture().get();
    assertEquals("translated by plugin", result.translatedText());
    assertEquals("plugin-echo", result.provider());
  }

  @Test
  void loadsServiceLoaderTranslationBackendProvidersFromInstalledPlugins() throws Exception {
    Path runtimeConfigDirectory = Files.createDirectories(tempDir.resolve("config-home/ircafe"));
    Path pluginDir = Files.createDirectories(runtimeConfigDirectory.resolve("plugins"));
    CompiledPluginJarSupport.writePluginJar(
        pluginDir.resolve("example-translation-backend-provider.jar"),
        SPI_BACKEND_CLASS,
        pluginBackendProviderSource(),
        MessageTranslationBackendProvider.class.getName(),
        CompiledPluginJarSupport.compatibleManifest(
            "example-translation-backend-provider", "1.0.0"));
    RuntimeConfigPathPort runtimeConfigPathPort =
        () -> runtimeConfigDirectory.resolve("ircafe.yml");

    InstalledPluginServices installedPlugins = new InstalledPluginServices(runtimeConfigPathPort);
    MessageTranslationBackendRegistry registry =
        new MessageTranslationBackendRegistry(List.of(), installedPlugins);

    assertTrue(installedPlugins.pluginProblems().isEmpty());
    MessageTranslationBackendProvider backend = registry.find("PLUGIN-SPI-ECHO").orElseThrow();
    MessageTranslationResult result = backend.translate(null).toCompletableFuture().get();
    assertEquals("translated by spi plugin", result.translatedText());
    assertEquals("plugin-spi-echo", result.provider());
  }

  @Test
  void rejectsDuplicateNormalizedIds() {
    List<MessageTranslationBackendProvider> backends =
        List.of(new StubBackend("DeepL"), new StubBackend(" deepl "));

    assertThrows(
        IllegalStateException.class, () -> new MessageTranslationBackendRegistry(backends));
  }

  @Test
  void rejectsBlankBackendIds() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new MessageTranslationBackendRegistry(List.of(new StubBackend(" "))));
  }

  private static String pluginBackendSource() {
    return """
        package plugin.translation;

        import cafe.woden.ircclient.app.translation.spi.MessageTranslationBackendProvider;
        import cafe.woden.ircclient.app.translation.MessageTranslationRequest;
        import cafe.woden.ircclient.app.translation.MessageTranslationResult;
        import java.util.concurrent.CompletableFuture;
        import java.util.concurrent.CompletionStage;

        public final class PluginEchoBackend implements MessageTranslationBackendProvider {
          @Override
          public String backendId() {
            return "plugin-echo";
          }

          @Override
          public CompletionStage<MessageTranslationResult> translate(
              MessageTranslationRequest request) {
            return CompletableFuture.completedFuture(
                new MessageTranslationResult(
                    "translated by plugin", "en", "fr", "plugin-echo"));
          }
        }
        """;
  }

  private static String pluginBackendProviderSource() {
    return """
        package plugin.translation;

        import cafe.woden.ircclient.app.translation.MessageTranslationRequest;
        import cafe.woden.ircclient.app.translation.MessageTranslationResult;
        import cafe.woden.ircclient.app.translation.spi.MessageTranslationBackendProvider;
        import java.util.concurrent.CompletableFuture;
        import java.util.concurrent.CompletionStage;

        public final class PluginSpiEchoBackend implements MessageTranslationBackendProvider {
          @Override
          public String backendId() {
            return "plugin-spi-echo";
          }

          @Override
          public CompletionStage<MessageTranslationResult> translate(
              MessageTranslationRequest request) {
            return CompletableFuture.completedFuture(
                new MessageTranslationResult(
                    "translated by spi plugin", "en", "fr", "plugin-spi-echo"));
          }
        }
        """;
  }

  private static final class RecordingInstalledPluginsPort implements InstalledPluginsPort {
    private final List<MessageTranslationBackendProvider> pluginBackends;

    private RecordingInstalledPluginsPort(List<MessageTranslationBackendProvider> pluginBackends) {
      this.pluginBackends = List.copyOf(pluginBackends);
    }

    @Override
    public <T> List<T> loadInstalledServices(Class<T> serviceType, List<T> builtInServices) {
      ArrayList<T> services = new ArrayList<>(builtInServices);
      if (serviceType == MessageTranslationBackendProvider.class) {
        for (MessageTranslationBackendProvider backend : pluginBackends) {
          services.add(serviceType.cast(backend));
        }
      }
      return List.copyOf(services);
    }
  }

  private record StubBackend(String backendId) implements MessageTranslationBackendProvider {
    @Override
    public CompletionStage<MessageTranslationResult> translate(MessageTranslationRequest request) {
      return CompletableFuture.completedFuture(new MessageTranslationResult("", "", "", ""));
    }
  }
}
