package cafe.woden.ircclient.ui.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.config.api.RuntimeConfigPathPort;
import cafe.woden.ircclient.config.plugins.InstalledPluginServices;
import cafe.woden.ircclient.util.CompiledPluginJarSupport;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CompositeMessageInputWordSuggestionProviderTest {

  private static final String PLUGIN_PROVIDER_CLASS = "plugin.input.PluginWordSuggestionProvider";

  @TempDir Path tempDir;

  @Test
  void mergesBuiltInAndPluginSuggestionsThroughInstalledPluginPort() {
    MessageInputWordSuggestionProvider builtIn = new StaticSuggestions("hello", "help");
    MessageInputWordSuggestionProvider plugin = new StaticSuggestions("help", "helium", "hero");
    MessageInputWordSuggestionProvider composite =
        CompositeMessageInputWordSuggestionProvider.from(
            builtIn, new RecordingInstalledPluginsPort(List.of(plugin)));

    assertEquals(List.of("hello", "help", "helium"), composite.suggestWords("he", 3));
  }

  @Test
  void ignoresFailingPluginSuggestionProviders() {
    MessageInputWordSuggestionProvider builtIn = new StaticSuggestions("hello");
    MessageInputWordSuggestionProvider failing =
        (token, maxSuggestions) -> {
          throw new IllegalStateException("boom");
        };
    MessageInputWordSuggestionProvider plugin = new StaticSuggestions("help");
    MessageInputWordSuggestionProvider composite =
        CompositeMessageInputWordSuggestionProvider.from(
            builtIn, new RecordingInstalledPluginsPort(List.of(failing, plugin)));

    assertEquals(List.of("hello", "help"), composite.suggestWords("he", 4));
  }

  @Test
  void loadsServiceLoaderWordSuggestionProvidersFromInstalledPlugins() throws Exception {
    Path runtimeConfigDirectory = Files.createDirectories(tempDir.resolve("config-home/ircafe"));
    Path pluginDir = Files.createDirectories(runtimeConfigDirectory.resolve("plugins"));
    CompiledPluginJarSupport.writePluginJar(
        pluginDir.resolve("example-word-suggestions.jar"),
        PLUGIN_PROVIDER_CLASS,
        pluginProviderSource(),
        MessageInputWordSuggestionProvider.class.getName(),
        CompiledPluginJarSupport.compatibleManifest("example-word-suggestions", "1.0.0"));
    RuntimeConfigPathPort runtimeConfigPathPort =
        () -> runtimeConfigDirectory.resolve("ircafe.yml");

    InstalledPluginServices installedPlugins = new InstalledPluginServices(runtimeConfigPathPort);
    MessageInputWordSuggestionProvider composite =
        CompositeMessageInputWordSuggestionProvider.from(
            new StaticSuggestions("built"), installedPlugins);

    assertTrue(installedPlugins.pluginProblems().isEmpty());
    assertEquals(List.of("built", "plugin-alpha", "plugin-beta"), composite.suggestWords("pl", 8));
  }

  @Test
  void asyncSuggestionsMergeInProviderOrder() throws Exception {
    MessageInputWordSuggestionProvider first =
        new AsyncSuggestions(CompletableFuture.completedFuture(List.of("hello", "help")));
    MessageInputWordSuggestionProvider second =
        new AsyncSuggestions(CompletableFuture.completedFuture(List.of("helium", "help")));
    MessageInputWordSuggestionProvider composite =
        CompositeMessageInputWordSuggestionProvider.from(
            first, new RecordingInstalledPluginsPort(List.of(second)));

    assertEquals(List.of("hello", "help", "helium"), composite.suggestWordsAsync("he", 5).get());
  }

  private static String pluginProviderSource() {
    return """
        package plugin.input;

        import cafe.woden.ircclient.ui.input.MessageInputWordSuggestionProvider;
        import java.util.List;

        public final class PluginWordSuggestionProvider
    implements MessageInputWordSuggestionProvider {
          @Override
          public List<String> suggestWords(String token, int maxSuggestions) {
            return List.of("plugin-alpha", "plugin-beta");
          }
        }
        """;
  }

  private record StaticSuggestions(List<String> suggestions)
      implements MessageInputWordSuggestionProvider {
    StaticSuggestions(String... suggestions) {
      this(List.of(suggestions));
    }

    @Override
    public List<String> suggestWords(String token, int maxSuggestions) {
      return suggestions;
    }
  }

  private record AsyncSuggestions(CompletableFuture<List<String>> suggestions)
      implements MessageInputWordSuggestionProvider {
    @Override
    public List<String> suggestWords(String token, int maxSuggestions) {
      return suggestions.join();
    }

    @Override
    public CompletableFuture<List<String>> suggestWordsAsync(String token, int maxSuggestions) {
      return suggestions;
    }
  }

  private static final class RecordingInstalledPluginsPort implements InstalledPluginsPort {
    private final List<MessageInputWordSuggestionProvider> pluginProviders;

    private RecordingInstalledPluginsPort(
        List<MessageInputWordSuggestionProvider> pluginProviders) {
      this.pluginProviders = List.copyOf(pluginProviders);
    }

    @Override
    public <T> List<T> loadInstalledServices(Class<T> serviceType, List<T> builtInServices) {
      ArrayList<T> services = new ArrayList<>(builtInServices);
      if (serviceType == MessageInputWordSuggestionProvider.class) {
        for (MessageInputWordSuggestionProvider provider : pluginProviders) {
          services.add(serviceType.cast(provider));
        }
      }
      return List.copyOf(services);
    }
  }
}
