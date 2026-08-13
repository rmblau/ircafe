package cafe.woden.ircclient.app.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.app.commands.spi.BackendNamedCommandHandler;
import cafe.woden.ircclient.app.commands.spi.BackendNamedCommandParseResult;
import cafe.woden.ircclient.app.commands.spi.BuiltInBackendNamedCommandNames;
import cafe.woden.ircclient.app.commands.spi.SlashCommandDescriptor;
import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.config.api.RuntimeConfigPathPort;
import cafe.woden.ircclient.util.CompiledPluginJarSupport;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BackendNamedCommandCatalogTest {

  private static final String SPI_PLUGIN_HANDLER_CLASS =
      "plugin.commands.PluginSpiBackendNamedCommandHandler";

  @TempDir Path tempDir;

  @Test
  void loadsParserProvidersFromInstalledPluginsPort() {
    BackendNamedCommandCatalog catalog =
        new BackendNamedCommandCatalog(
            new FakeInstalledPluginsPort(List.of(new PluginProvidedBackendNamedCommandHandler())));

    ParsedInput parsed = catalog.parse("/backendping hello");

    assertTrue(parsed instanceof ParsedInput.BackendNamed);
    assertEquals("backendping", ((ParsedInput.BackendNamed) parsed).command());
    assertEquals("hello", ((ParsedInput.BackendNamed) parsed).args());
  }

  @Test
  void installedPluginPortConstructorLoadsApplicationClasspathHandlersWithoutSpringSeed() {
    BackendNamedCommandCatalog catalog =
        new BackendNamedCommandCatalog(new FakeInstalledPluginsPort(List.of()));

    ParsedInput parsed = catalog.parse("/qnet list");

    assertTrue(parsed instanceof ParsedInput.BackendNamed);
    assertEquals(
        BuiltInBackendNamedCommandNames.QUASSEL_NETWORK,
        ((ParsedInput.BackendNamed) parsed).command());
    assertEquals("list", ((ParsedInput.BackendNamed) parsed).args());
  }

  @Test
  void loadsBuiltInBackendNamedHandlersFromApplicationClasspath() {
    BackendNamedCommandCatalog catalog =
        BackendNamedCommandCatalog.installed(
            (cafe.woden.ircclient.config.api.RuntimeConfigPathPort) null,
            BackendNamedCommandCatalogTest.class.getClassLoader());

    ParsedInput parsed = catalog.parse("/qnet list");

    assertTrue(parsed instanceof ParsedInput.BackendNamed);
    assertEquals(
        BuiltInBackendNamedCommandNames.QUASSEL_NETWORK,
        ((ParsedInput.BackendNamed) parsed).command());
    assertEquals("list", ((ParsedInput.BackendNamed) parsed).args());
  }

  @Test
  void loadsServiceProvidersFromPluginDirectoryJar() throws Exception {
    Path pluginDir = Files.createDirectories(tempDir.resolve("plugins"));
    writePluginJar(pluginDir.resolve("backendping.jar"));

    BackendNamedCommandCatalog catalog =
        BackendNamedCommandCatalog.installed(
            pluginDir, BackendNamedCommandCatalogTest.class.getClassLoader());

    ParsedInput parsed = catalog.parse("/backendping hello");

    assertTrue(parsed instanceof ParsedInput.BackendNamed);
    assertEquals("backendping", ((ParsedInput.BackendNamed) parsed).command());
    assertEquals("hello", ((ParsedInput.BackendNamed) parsed).args());
  }

  @Test
  void loadsServiceProviderSpiFromPluginDirectoryJar() throws Exception {
    Path pluginDir = Files.createDirectories(tempDir.resolve("plugins"));
    CompiledPluginJarSupport.writePluginJar(
        pluginDir.resolve("backendping-spi.jar"),
        SPI_PLUGIN_HANDLER_CLASS,
        pluginSpiHandlerSource(),
        cafe.woden.ircclient.app.commands.spi.BackendNamedCommandHandler.class.getName(),
        CompiledPluginJarSupport.compatibleManifest("backend-named-command-spi-test", "1.0.0"));

    BackendNamedCommandCatalog catalog =
        BackendNamedCommandCatalog.installed(
            pluginDir, BackendNamedCommandCatalogTest.class.getClassLoader());

    ParsedInput parsed = catalog.parse("/backendping hello");

    assertTrue(parsed instanceof ParsedInput.BackendNamed);
    assertEquals("backendping", ((ParsedInput.BackendNamed) parsed).command());
    assertEquals("hello", ((ParsedInput.BackendNamed) parsed).args());
  }

  @Test
  void loadsServiceProvidersFromPluginsNextToRuntimeConfig() throws Exception {
    Path runtimeConfigDirectory = Files.createDirectories(tempDir.resolve("config-home/ircafe"));
    Path pluginDir = Files.createDirectories(runtimeConfigDirectory.resolve("plugins"));
    writePluginJar(pluginDir.resolve("backendping.jar"));
    RuntimeConfigPathPort runtimeConfigPathPort =
        () -> runtimeConfigDirectory.resolve("ircafe.yml");

    BackendNamedCommandCatalog catalog =
        BackendNamedCommandCatalog.installed(
            runtimeConfigPathPort, BackendNamedCommandCatalogTest.class.getClassLoader());

    ParsedInput parsed = catalog.parse("/backendping hello");

    assertEquals(
        runtimeConfigDirectory.resolve("plugins"),
        BackendNamedCommandCatalog.resolvePluginDirectory(runtimeConfigPathPort));
    assertTrue(parsed instanceof ParsedInput.BackendNamed);
    assertEquals("backendping", ((ParsedInput.BackendNamed) parsed).command());
    assertEquals("hello", ((ParsedInput.BackendNamed) parsed).args());
  }

  @Test
  void duplicateParserCommandRegistrationsFailFast() {
    BackendNamedCommandHandler first =
        new BackendNamedCommandHandler() {
          @Override
          public Set<String> supportedCommandNames() {
            return Set.of("backendping");
          }

          @Override
          public BackendNamedCommandParseResult parse(String line, String matchedCommandName) {
            return null;
          }
        };
    BackendNamedCommandHandler second =
        new BackendNamedCommandHandler() {
          @Override
          public Set<String> supportedCommandNames() {
            return Set.of("backendping");
          }

          @Override
          public BackendNamedCommandParseResult parse(String line, String matchedCommandName) {
            return null;
          }
        };

    assertThrows(
        IllegalStateException.class,
        () -> BackendNamedCommandCatalog.fromHandlers(List.of(first, second)));
  }

  private static void writePluginJar(Path jarPath) throws Exception {
    CompiledPluginJarSupport.writePluginJar(
        jarPath,
        SPI_PLUGIN_HANDLER_CLASS,
        pluginSpiHandlerSource(),
        BackendNamedCommandHandler.class.getName(),
        CompiledPluginJarSupport.compatibleManifest("backend-named-command-test", "1.0.0"));
  }

  private static String pluginSpiHandlerSource() {
    return """
        package plugin.commands;

        import cafe.woden.ircclient.app.commands.spi.BackendNamedCommandParseResult;
        import cafe.woden.ircclient.app.commands.spi.SlashCommandDescriptor;
        import cafe.woden.ircclient.app.commands.spi.BackendNamedCommandHandler;
        import java.util.List;
        import java.util.Set;

        public final class PluginSpiBackendNamedCommandHandler
            implements BackendNamedCommandHandler {
          @Override
          public Set<String> supportedCommandNames() {
            return Set.of("backendping");
          }

          @Override
          public BackendNamedCommandParseResult parse(String line, String matchedCommandName) {
            String commandToken = "/" + matchedCommandName;
            String args = line != null && line.length() > commandToken.length()
                ? line.substring(commandToken.length()).trim()
                : "";
            return new BackendNamedCommandParseResult(
                matchedCommandName,
                args);
          }

          @Override
          public List<SlashCommandDescriptor> autocompleteCommands() {
            return List.of(new SlashCommandDescriptor("/backendping", "Plugin test command"));
          }
        }
        """;
  }

  private static final class FakeInstalledPluginsPort implements InstalledPluginsPort {
    private final List<?> pluginServices;

    private FakeInstalledPluginsPort(List<?> pluginServices) {
      this.pluginServices = List.copyOf(pluginServices);
    }

    @Override
    public <T> List<T> loadInstalledServices(Class<T> serviceType, List<T> builtInServices) {
      ArrayList<T> services =
          new ArrayList<>(java.util.Objects.requireNonNullElse(builtInServices, List.of()));
      for (Object pluginService : pluginServices) {
        if (serviceType.isInstance(pluginService)) {
          services.add(serviceType.cast(pluginService));
        }
      }
      return List.copyOf(services);
    }
  }

  public static final class PluginProvidedBackendNamedCommandHandler
      implements BackendNamedCommandHandler {

    @Override
    public Set<String> supportedCommandNames() {
      return Set.of("backendping");
    }

    @Override
    public BackendNamedCommandParseResult parse(String line, String matchedCommandName) {
      return new BackendNamedCommandParseResult(
          matchedCommandName, BackendNamedCommandParser.argAfter(line, "/" + matchedCommandName));
    }

    @Override
    public List<SlashCommandDescriptor> autocompleteCommands() {
      return List.of(new SlashCommandDescriptor("/backendping", "Plugin test command"));
    }
  }
}
