package cafe.woden.ircclient.ui.chat.embed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.config.api.RuntimeConfigPathPort;
import cafe.woden.ircclient.config.plugins.InstalledPluginServices;
import cafe.woden.ircclient.util.CompiledPluginJarSupport;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PreviewHttpHeaderProviderPluginTest {

  @TempDir Path tempDir;

  @Test
  void loadsPreviewHttpHeadersFromPluginDirectoryJar() throws Exception {
    Path runtimeConfigDirectory = Files.createDirectories(tempDir.resolve("config-home/ircafe"));
    Path pluginDir = Files.createDirectories(runtimeConfigDirectory.resolve("plugins"));
    writePluginJar(pluginDir.resolve("plugin-preview-http-header.jar"));
    RuntimeConfigPathPort runtimeConfigPathPort =
        () -> runtimeConfigDirectory.resolve("ircafe.yml");
    InstalledPluginServices installedPlugins = new InstalledPluginServices(runtimeConfigPathPort);

    List<PreviewHttpHeaderProvider> providers =
        installedPlugins.loadInstalledServices(PreviewHttpHeaderProvider.class, List.of());
    Map<String, String> headers =
        PreviewHttp.headersFor(
            URI.create("https://cards.example.test/article"),
            "text/html",
            Map.of("X-Explicit", "resolver"),
            providers);

    assertEquals("https://cards.example.test/", headers.get(PreviewHttp.HEADER_REFERER));
    assertEquals("IRCafe Preview Plugin", headers.get("X-Preview-Plugin"));
    assertEquals("resolver", headers.get("X-Explicit"));
    assertTrue(installedPlugins.pluginProblems().isEmpty());
  }

  private void writePluginJar(Path jarPath) throws Exception {
    String providerClassName = "cafe.woden.ircclient.testplugins.PluginPreviewHttpHeaders";
    String providerSource =
        """
        package cafe.woden.ircclient.testplugins;

        import cafe.woden.ircclient.ui.chat.embed.PreviewHttpHeaderProvider;
        import java.net.URI;
        import java.util.Map;

        public final class PluginPreviewHttpHeaders implements PreviewHttpHeaderProvider {
          @Override
          public Map<String, String> previewHttpHeaders(URI uri) {
            if (uri == null || !\"cards.example.test\".equals(uri.getHost())) {
              return Map.of();
            }
            return Map.of(
                \"Referer\", \"https://cards.example.test/\",
                \"X-Preview-Plugin\", \"IRCafe Preview Plugin\");
          }
        }
        """;
    CompiledPluginJarSupport.writePluginJar(
        jarPath,
        providerClassName,
        providerSource,
        PreviewHttpHeaderProvider.class.getName(),
        CompiledPluginJarSupport.compatibleManifest("plugin-preview-http-header", "1.0.0"));
  }
}
