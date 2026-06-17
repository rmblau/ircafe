package cafe.woden.ircclient.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.config.api.RuntimeConfigPathPort;
import cafe.woden.ircclient.config.plugins.InstalledPluginServices;
import cafe.woden.ircclient.util.CompiledPluginJarSupport;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ExternalBrowserSchemeProviderPluginTest {

  @TempDir Path tempDir;

  @Test
  void loadsExternalBrowserSchemesFromPluginDirectoryJar() throws Exception {
    Path runtimeConfigDirectory = Files.createDirectories(tempDir.resolve("config-home/ircafe"));
    Path pluginDir = Files.createDirectories(runtimeConfigDirectory.resolve("plugins"));
    writePluginJar(pluginDir.resolve("plugin-browser-scheme.jar"));
    RuntimeConfigPathPort runtimeConfigPathPort =
        () -> runtimeConfigDirectory.resolve("ircafe.yml");
    InstalledPluginServices installedPlugins = new InstalledPluginServices(runtimeConfigPathPort);
    TestableLauncher launcher = new TestableLauncher("linux", installedPlugins);
    launcher.succeedCommandPrefix = "xdg-open";

    assertTrue(launcher.open("gemini://gemini.example/docs"));
    assertEquals("xdg-open gemini://gemini.example/docs", launcher.attemptedCommands.get(0));
    assertTrue(installedPlugins.pluginProblems().isEmpty());
  }

  private void writePluginJar(Path jarPath) throws Exception {
    String providerClassName = "cafe.woden.ircclient.testplugins.PluginBrowserSchemeProvider";
    String providerSource =
        pluginProviderSource("cafe.woden.ircclient.ui.spi.ExternalBrowserSchemeProvider");
    CompiledPluginJarSupport.writePluginJar(
        jarPath,
        providerClassName,
        providerSource,
        cafe.woden.ircclient.ui.spi.ExternalBrowserSchemeProvider.class.getName(),
        CompiledPluginJarSupport.compatibleManifest("plugin-browser-scheme", "1.0.0"));
  }

  private static String pluginProviderSource(String providerImport) {
    return """
        package cafe.woden.ircclient.testplugins;

        import %s;
        import java.util.Set;

        public final class PluginBrowserSchemeProvider implements ExternalBrowserSchemeProvider {
          @Override
          public Set<String> allowedSchemes() {
            return Set.of("gemini");
          }
        }
        """
        .formatted(providerImport);
  }

  private static final class TestableLauncher extends ExternalBrowserLauncher {
    private final String osLower;
    private final List<String> attemptedCommands = new ArrayList<>();
    private String succeedCommandPrefix;

    private TestableLauncher(String osLower, InstalledPluginServices installedPlugins) {
      super(installedPlugins);
      this.osLower = osLower;
    }

    @Override
    protected String currentOsLowerCase() {
      return osLower;
    }

    @Override
    protected boolean tryStart(String... cmd) {
      String joined = String.join(" ", Arrays.asList(cmd));
      attemptedCommands.add(joined);
      return succeedCommandPrefix != null && cmd.length > 0 && cmd[0].equals(succeedCommandPrefix);
    }
  }
}
