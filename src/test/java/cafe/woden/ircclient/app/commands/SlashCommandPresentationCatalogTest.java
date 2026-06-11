package cafe.woden.ircclient.app.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.config.api.RuntimeConfigPathPort;
import cafe.woden.ircclient.config.plugins.InstalledPluginServices;
import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.util.CompiledPluginJarSupport;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SlashCommandPresentationCatalogTest {

  private static final String PLUGIN_CONTRIBUTOR_CLASS = "plugin.commands.PluginHelpContributor";

  @TempDir Path tempDir;

  @Test
  void mergesContributorAndBackendAutocompleteCommands() {
    SlashCommandPresentationContributor builtInContributor =
        new SlashCommandPresentationContributor() {
          @Override
          public List<SlashCommandDescriptor> autocompleteCommands() {
            return List.of(new SlashCommandDescriptor("/join", "Join channel"));
          }
        };
    BackendNamedCommandHandler backendHandler =
        new BackendNamedCommandHandler() {
          @Override
          public Set<String> supportedCommandNames() {
            return Set.of("backendping");
          }

          @Override
          public ParsedInput parse(String line, String matchedCommandName) {
            return new ParsedInput.BackendNamed("backendping", "");
          }

          @Override
          public List<SlashCommandDescriptor> autocompleteCommands() {
            return List.of(new SlashCommandDescriptor("/backendping", "Plugin test command"));
          }
        };

    SlashCommandPresentationCatalog catalog =
        new SlashCommandPresentationCatalog(
            List.of(builtInContributor),
            BackendNamedCommandCatalog.fromHandlers(List.of(backendHandler)));

    List<String> commands =
        catalog.autocompleteCommands().stream().map(SlashCommandDescriptor::command).toList();

    assertTrue(commands.contains("/join"));
    assertTrue(commands.contains("/backendping"));
  }

  @Test
  void includesPresentationContributorsLoadedThroughInstalledPluginPort() {
    SlashCommandPresentationContributor builtInContributor =
        autocompleteContributor("/built-in", "Built-in command");
    SlashCommandPresentationContributor pluginContributor =
        autocompleteContributor("/plugin-help", "Plugin command");

    SlashCommandPresentationCatalog catalog =
        new SlashCommandPresentationCatalog(
            List.of(builtInContributor),
            BackendNamedCommandCatalog.empty(),
            new RecordingInstalledPluginsPort(List.of(pluginContributor)));

    List<String> commands =
        catalog.autocompleteCommands().stream().map(SlashCommandDescriptor::command).toList();

    assertEquals(List.of("/built-in", "/plugin-help"), commands);
  }

  @Test
  void loadsServiceLoaderPresentationContributorsFromInstalledPlugins() throws Exception {
    Path runtimeConfigDirectory = Files.createDirectories(tempDir.resolve("config-home/ircafe"));
    Path pluginDir = Files.createDirectories(runtimeConfigDirectory.resolve("plugins"));
    CompiledPluginJarSupport.writePluginJar(
        pluginDir.resolve("example-slash-command-presentation.jar"),
        PLUGIN_CONTRIBUTOR_CLASS,
        pluginContributorSource(),
        SlashCommandPresentationContributor.class.getName(),
        CompiledPluginJarSupport.compatibleManifest(
            "example-slash-command-presentation", "1.0.0"));
    RuntimeConfigPathPort runtimeConfigPathPort =
        () -> runtimeConfigDirectory.resolve("ircafe.yml");

    InstalledPluginServices installedPlugins = new InstalledPluginServices(runtimeConfigPathPort);
    SlashCommandPresentationCatalog catalog =
        new SlashCommandPresentationCatalog(
            List.of(), BackendNamedCommandCatalog.empty(), installedPlugins);

    assertTrue(installedPlugins.pluginProblems().isEmpty());
    List<String> commands =
        catalog.autocompleteCommands().stream().map(SlashCommandDescriptor::command).toList();
    assertEquals(List.of("/plugin-help"), commands);
  }

  @Test
  void backendTopicHelpLinesAreExposedThroughCatalogHandlers() {
    BackendNamedCommandHandler backendHandler =
        new BackendNamedCommandHandler() {
          @Override
          public Set<String> supportedCommandNames() {
            return Set.of("backendping");
          }

          @Override
          public ParsedInput parse(String line, String matchedCommandName) {
            return new ParsedInput.BackendNamed("backendping", "");
          }

          @Override
          public Map<String, List<String>> topicHelpLines() {
            return Map.of("backendping", List.of("/backendping <arg>"));
          }
        };
    SlashCommandPresentationCatalog catalog =
        new SlashCommandPresentationCatalog(
            List.of(), BackendNamedCommandCatalog.fromHandlers(List.of(backendHandler)));
    AtomicReference<String> rendered = new AtomicReference<>();

    catalog
        .topicHelpHandlers((target, line) -> rendered.set(target.target() + ":" + line))
        .get("backendping")
        .accept(new TargetRef("libera", "status"));

    assertEquals("status:/backendping <arg>", rendered.get());
  }

  private static SlashCommandPresentationContributor autocompleteContributor(
      String command, String summary) {
    return new SlashCommandPresentationContributor() {
      @Override
      public List<SlashCommandDescriptor> autocompleteCommands() {
        return List.of(new SlashCommandDescriptor(command, summary));
      }
    };
  }

  private static String pluginContributorSource() {
    return """
        package plugin.commands;

        import cafe.woden.ircclient.app.commands.SlashCommandDescriptor;
        import cafe.woden.ircclient.app.commands.SlashCommandPresentationContributor;
        import java.util.List;

        public final class PluginHelpContributor implements SlashCommandPresentationContributor {
          @Override
          public List<SlashCommandDescriptor> autocompleteCommands() {
            return List.of(new SlashCommandDescriptor("/plugin-help", "Plugin help command"));
          }
        }
        """;
  }

  private static final class RecordingInstalledPluginsPort implements InstalledPluginsPort {
    private final List<SlashCommandPresentationContributor> pluginContributors;

    private RecordingInstalledPluginsPort(
        List<SlashCommandPresentationContributor> pluginContributors) {
      this.pluginContributors = List.copyOf(pluginContributors);
    }

    @Override
    public <T> List<T> loadInstalledServices(Class<T> serviceType, List<T> builtInServices) {
      ArrayList<T> services = new ArrayList<>(builtInServices);
      if (serviceType == SlashCommandPresentationContributor.class) {
        for (SlashCommandPresentationContributor contributor : pluginContributors) {
          services.add(serviceType.cast(contributor));
        }
      }
      return List.copyOf(services);
    }
  }
}
