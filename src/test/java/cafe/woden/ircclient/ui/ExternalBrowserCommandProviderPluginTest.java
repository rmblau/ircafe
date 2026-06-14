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

class ExternalBrowserCommandProviderPluginTest {

  @TempDir Path tempDir;

  @Test
  void loadsExternalBrowserCommandsFromPluginDirectoryJar() throws Exception {
    Path runtimeConfigDirectory = Files.createDirectories(tempDir.resolve("config-home/ircafe"));
    Path pluginDir = Files.createDirectories(runtimeConfigDirectory.resolve("plugins"));
    writePluginJar(pluginDir.resolve("plugin-browser-command.jar"));
    RuntimeConfigPathPort runtimeConfigPathPort =
        () -> runtimeConfigDirectory.resolve("ircafe.yml");
    InstalledPluginServices installedPlugins = new InstalledPluginServices(runtimeConfigPathPort);
    TestableLauncher launcher = new TestableLauncher("linux", installedPlugins);
    launcher.succeedCommandPrefix = "plugin-browser";

    assertTrue(launcher.open("https://example.com/releases"));
    assertEquals(
        "plugin-browser https://example.com/releases linux", launcher.attemptedCommands.get(0));
    assertTrue(installedPlugins.pluginProblems().isEmpty());
  }

  private void writePluginJar(Path jarPath) throws Exception {
    String providerClassName = "cafe.woden.ircclient.testplugins.PluginBrowserCommandProvider";
    String providerSource =
        """
        package cafe.woden.ircclient.testplugins;

        import cafe.woden.ircclient.ui.ExternalBrowserCommandProvider;
        import java.util.List;

        public final class PluginBrowserCommandProvider implements ExternalBrowserCommandProvider {
          @Override
          public List<List<String>> browserCommands(String normalizedUrl, String osName) {
            return List.of(List.of("plugin-browser", normalizedUrl, osName));
          }
        }
        """;
    CompiledPluginJarSupport.writePluginJar(
        jarPath,
        providerClassName,
        providerSource,
        ExternalBrowserCommandProvider.class.getName(),
        CompiledPluginJarSupport.compatibleManifest("plugin-browser-command", "1.0.0"));
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
