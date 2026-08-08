package cafe.woden.ircclient.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.config.api.RuntimeConfigPathPort;
import cafe.woden.ircclient.config.plugins.InstalledPluginServices;
import cafe.woden.ircclient.ui.spi.ExternalBrowserCommandProvider;
import cafe.woden.ircclient.ui.spi.ExternalBrowserSchemeProvider;
import cafe.woden.ircclient.util.CompiledPluginJarSupport;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ExternalBrowserProviderGuideFixtureTest {

  private static final String GUIDE_SCHEME_PROVIDER_CLASS =
      "example.browser.ExampleBrowserSchemeProvider";
  private static final String GUIDE_COMMAND_PROVIDER_CLASS =
      "example.browser.ExampleBrowserCommandProvider";

  @TempDir Path tempDir;

  @Test
  void documentedExternalBrowserPluginCanPackageSchemeAndCommandProviders() throws Exception {
    Path runtimeConfigDirectory = Files.createDirectories(tempDir.resolve("config-home/ircafe"));
    Path pluginDir = Files.createDirectories(runtimeConfigDirectory.resolve("plugins"));
    CompiledPluginJarSupport.writePluginJar(
        pluginDir.resolve("external-browser-guide-example.jar"),
        Map.of(
            GUIDE_SCHEME_PROVIDER_CLASS, guideSchemeProviderSource(),
            GUIDE_COMMAND_PROVIDER_CLASS, guideCommandProviderSource()),
        Map.of(
            ExternalBrowserSchemeProvider.class.getName(),
            List.of(GUIDE_SCHEME_PROVIDER_CLASS),
            ExternalBrowserCommandProvider.class.getName(),
            List.of(GUIDE_COMMAND_PROVIDER_CLASS)),
        CompiledPluginJarSupport.compatibleManifest("external-browser-guide-example", "1.0.0"));
    RuntimeConfigPathPort runtimeConfigPathPort =
        () -> runtimeConfigDirectory.resolve("ircafe.yml");

    InstalledPluginServices installedPlugins = new InstalledPluginServices(runtimeConfigPathPort);
    TestableLauncher launcher = new TestableLauncher("linux", installedPlugins);
    launcher.succeedCommandPrefix = "plugin-browser";

    assertTrue(installedPlugins.pluginProblems().isEmpty());
    assertTrue(launcher.open("gemini://gemini.example/docs"));
    assertEquals(
        "plugin-browser gemini://gemini.example/docs linux", launcher.attemptedCommands.getFirst());
  }

  private static String guideSchemeProviderSource() {
    return """
        package example.browser;

        import cafe.woden.ircclient.ui.spi.ExternalBrowserSchemeProvider;
        import java.util.Set;

        public final class ExampleBrowserSchemeProvider implements ExternalBrowserSchemeProvider {
          @Override
          public Set<String> allowedSchemes() {
            return Set.of("gemini");
          }
        }
        """;
  }

  private static String guideCommandProviderSource() {
    return """
        package example.browser;

        import cafe.woden.ircclient.ui.spi.ExternalBrowserCommandProvider;
        import java.util.List;

        public final class ExampleBrowserCommandProvider implements ExternalBrowserCommandProvider {
          @Override
          public List<List<String>> browserCommands(String normalizedUrl, String osName) {
            if (!"linux".equals(osName)) {
              return List.of();
            }
            return List.of(List.of("plugin-browser", normalizedUrl, osName));
          }
        }
        """;
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
