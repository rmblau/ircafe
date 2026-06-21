package cafe.woden.ircclient.app.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.app.commands.spi.BackendNamedCommandHandler;
import cafe.woden.ircclient.app.commands.spi.BackendNamedCommandParseResult;
import cafe.woden.ircclient.app.commands.spi.SlashCommandDescriptor;
import cafe.woden.ircclient.app.commands.spi.SlashCommandHelpSink;
import cafe.woden.ircclient.app.commands.spi.SlashCommandPresentationContributor;
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
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SlashCommandPresentationCatalogTest {

  private static final String PLUGIN_CONTRIBUTOR_CLASS = "plugin.commands.PluginHelpContributor";
  private static final String SPI_PLUGIN_CONTRIBUTOR_CLASS =
      "plugin.commands.PluginSpiHelpContributor";

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
          public BackendNamedCommandParseResult parse(String line, String matchedCommandName) {
            return new BackendNamedCommandParseResult("backendping", "");
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
  void presentationContributorsCanRenderGeneralHelpThroughLineAppender() {
    SlashCommandPresentationContributor contributor =
        new SlashCommandPresentationContributor() {
          @Override
          public void appendGeneralHelp(SlashCommandHelpSink help) {
            help.appendLine("/plugin-help - plugin-provided help");
          }
        };
    SlashCommandPresentationCatalog catalog =
        new SlashCommandPresentationCatalog(
            List.of(contributor), BackendNamedCommandCatalog.empty());
    ArrayList<String> rendered = new ArrayList<>();

    catalog.appendGeneralHelp(
        new TargetRef("libera", "status"),
        (target, line) -> rendered.add(target.target() + ":" + line));

    assertEquals(List.of("status:/plugin-help - plugin-provided help"), rendered);
  }

  @Test
  void presentationContributorsCanReadPortableHelpTargetView() {
    SlashCommandPresentationContributor contributor =
        new SlashCommandPresentationContributor() {
          @Override
          public void appendGeneralHelp(SlashCommandHelpSink help) {
            help.appendLine(help.target().serverId() + ":" + help.target().target());
          }
        };
    SlashCommandPresentationCatalog catalog =
        new SlashCommandPresentationCatalog(
            List.of(contributor), BackendNamedCommandCatalog.empty());
    ArrayList<String> rendered = new ArrayList<>();

    catalog.appendGeneralHelp(
        new TargetRef("libera", "#java"), (target, line) -> rendered.add(line));

    assertEquals(List.of("libera:#java"), rendered);
  }

  @Test
  void presentationContributorsCanRenderTopicHelpThroughLineAppender() {
    SlashCommandPresentationContributor contributor =
        new SlashCommandPresentationContributor() {
          @Override
          public Map<String, Consumer<SlashCommandHelpSink>> topicHelpHandlers() {
            return Map.of("plugin-help", help -> help.appendLine("/plugin-help <arg>"));
          }
        };
    SlashCommandPresentationCatalog catalog =
        new SlashCommandPresentationCatalog(
            List.of(contributor), BackendNamedCommandCatalog.empty());
    AtomicReference<String> rendered = new AtomicReference<>();

    catalog
        .topicHelpHandlers((target, line) -> rendered.set(target.target() + ":" + line))
        .get("plugin-help")
        .accept(new TargetRef("libera", "status"));

    assertEquals("status:/plugin-help <arg>", rendered.get());
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
        CompiledPluginJarSupport.compatibleManifest("example-slash-command-presentation", "1.0.0"));
    RuntimeConfigPathPort runtimeConfigPathPort =
        () -> runtimeConfigDirectory.resolve("ircafe.yml");

    InstalledPluginServices installedPlugins = new InstalledPluginServices(runtimeConfigPathPort);
    SlashCommandPresentationCatalog catalog =
        new SlashCommandPresentationCatalog(
            List.of(), BackendNamedCommandCatalog.empty(), installedPlugins);

    assertTrue(installedPlugins.pluginProblems().isEmpty());
    List<String> commands =
        catalog.autocompleteCommands().stream().map(SlashCommandDescriptor::command).toList();
    assertTrue(commands.contains("/join"));
    assertTrue(commands.contains("/plugin-help"));

    ArrayList<String> generalHelp = new ArrayList<>();
    catalog.appendGeneralHelp(
        new TargetRef("libera", "status"),
        (target, line) -> generalHelp.add(target.target() + ":" + line));
    assertEquals(List.of("status:/plugin-help - plugin jar help"), generalHelp);

    AtomicReference<String> topicHelp = new AtomicReference<>();
    catalog
        .topicHelpHandlers((target, line) -> topicHelp.set(target.target() + ":" + line))
        .get("plugin-help")
        .accept(new TargetRef("libera", "status"));
    assertEquals("status:/plugin-help <arg>", topicHelp.get());
  }

  @Test
  void loadsServiceLoaderPresentationContributorProvidersFromInstalledPlugins() throws Exception {
    Path runtimeConfigDirectory = Files.createDirectories(tempDir.resolve("config-home/ircafe"));
    Path pluginDir = Files.createDirectories(runtimeConfigDirectory.resolve("plugins"));
    CompiledPluginJarSupport.writePluginJar(
        pluginDir.resolve("example-slash-command-presentation-spi.jar"),
        SPI_PLUGIN_CONTRIBUTOR_CLASS,
        pluginSpiContributorSource(),
        cafe.woden.ircclient.app.commands.spi.SlashCommandPresentationContributor.class.getName(),
        CompiledPluginJarSupport.compatibleManifest(
            "example-slash-command-presentation-spi", "1.0.0"));
    RuntimeConfigPathPort runtimeConfigPathPort =
        () -> runtimeConfigDirectory.resolve("ircafe.yml");

    InstalledPluginServices installedPlugins = new InstalledPluginServices(runtimeConfigPathPort);
    SlashCommandPresentationCatalog catalog =
        new SlashCommandPresentationCatalog(
            List.of(), BackendNamedCommandCatalog.empty(), installedPlugins);

    assertTrue(installedPlugins.pluginProblems().isEmpty());
    List<String> commands =
        catalog.autocompleteCommands().stream().map(SlashCommandDescriptor::command).toList();
    assertTrue(commands.contains("/join"));
    assertTrue(commands.contains("/plugin-help"));

    ArrayList<String> generalHelp = new ArrayList<>();
    catalog.appendGeneralHelp(
        new TargetRef("libera", "status"),
        (target, line) -> generalHelp.add(target.target() + ":" + line));
    assertEquals(List.of("status:/plugin-help - plugin jar help"), generalHelp);

    AtomicReference<String> topicHelp = new AtomicReference<>();
    catalog
        .topicHelpHandlers((target, line) -> topicHelp.set(target.target() + ":" + line))
        .get("plugin-help")
        .accept(new TargetRef("libera", "status"));
    assertEquals("status:/plugin-help <arg>", topicHelp.get());
  }

  @Test
  void loadsCorePresentationContributorThroughClasspathServiceLoader() {
    RuntimeConfigPathPort runtimeConfigPathPort = () -> tempDir.resolve("ircafe.yml");
    InstalledPluginServices installedPlugins = new InstalledPluginServices(runtimeConfigPathPort);
    SlashCommandPresentationCatalog catalog =
        new SlashCommandPresentationCatalog(
            List.of(), BackendNamedCommandCatalog.empty(), installedPlugins);

    assertTrue(installedPlugins.pluginProblems().isEmpty());
    List<String> commands =
        catalog.autocompleteCommands().stream().map(SlashCommandDescriptor::command).toList();
    assertTrue(commands.contains("/join"));
    assertTrue(commands.contains("/raw"));
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
          public BackendNamedCommandParseResult parse(String line, String matchedCommandName) {
            return new BackendNamedCommandParseResult("backendping", "");
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

        import cafe.woden.ircclient.app.commands.spi.SlashCommandDescriptor;
        import cafe.woden.ircclient.app.commands.spi.SlashCommandHelpSink;
        import cafe.woden.ircclient.app.commands.spi.SlashCommandPresentationContributor;
        import java.util.List;
        import java.util.Map;
        import java.util.function.Consumer;

        public final class PluginHelpContributor implements SlashCommandPresentationContributor {
          @Override
          public List<SlashCommandDescriptor> autocompleteCommands() {
            return List.of(new SlashCommandDescriptor("/plugin-help", "Plugin help command"));
          }

          @Override
          public void appendGeneralHelp(SlashCommandHelpSink help) {
            help.appendLine("/plugin-help - plugin jar help");
          }

          @Override
          public Map<String, Consumer<SlashCommandHelpSink>> topicHelpHandlers() {
            return Map.of("plugin-help", help -> help.appendLine("/plugin-help <arg>"));
          }
        }
        """;
  }

  private static String pluginSpiContributorSource() {
    return """
        package plugin.commands;

        import cafe.woden.ircclient.app.commands.spi.SlashCommandDescriptor;
        import cafe.woden.ircclient.app.commands.spi.SlashCommandHelpSink;
        import cafe.woden.ircclient.app.commands.spi.SlashCommandPresentationContributor;
        import java.util.List;
        import java.util.Map;
        import java.util.function.Consumer;

        public final class PluginSpiHelpContributor
            implements SlashCommandPresentationContributor {
          @Override
          public List<SlashCommandDescriptor> autocompleteCommands() {
            return List.of(new SlashCommandDescriptor("/plugin-help", "Plugin help command"));
          }

          @Override
          public void appendGeneralHelp(SlashCommandHelpSink help) {
            help.appendLine("/plugin-help - plugin jar help");
          }

          @Override
          public Map<String, Consumer<SlashCommandHelpSink>> topicHelpHandlers() {
            return Map.of("plugin-help", help -> help.appendLine("/plugin-help <arg>"));
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
