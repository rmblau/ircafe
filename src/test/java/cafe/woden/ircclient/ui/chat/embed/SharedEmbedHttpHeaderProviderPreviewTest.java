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

/** Preferred SPI coverage for plugin-contributed link-preview headers. */
class SharedEmbedHttpHeaderProviderPreviewTest {

  @TempDir Path tempDir;

  @Test
  void loadsPreviewHeadersFromSharedEmbedHeaderPluginJar() throws Exception {
    Path runtimeConfigDirectory = Files.createDirectories(tempDir.resolve("config-home/ircafe"));
    Path pluginDir = Files.createDirectories(runtimeConfigDirectory.resolve("plugins"));
    writePluginJar(pluginDir.resolve("plugin-shared-embed-preview-header.jar"));
    RuntimeConfigPathPort runtimeConfigPathPort =
        () -> runtimeConfigDirectory.resolve("ircafe.yml");
    InstalledPluginServices installedPlugins = new InstalledPluginServices(runtimeConfigPathPort);

    List<cafe.woden.ircclient.ui.chat.embed.spi.EmbedHttpHeaderProvider> providers =
        EmbedHttpHeaderProviders.loadInstalledProviders(installedPlugins);
    Map<String, String> headers =
        PreviewHttp.headersFor(
            URI.create("https://cards.example.test/article"),
            "text/html",
            Map.of("X-Explicit", "resolver"),
            providers);

    assertEquals("https://cards.example.test/", headers.get(PreviewHttp.HEADER_REFERER));
    assertEquals("IRCafe Shared Preview Plugin", headers.get("X-Embed-Plugin"));
    assertEquals("resolver", headers.get("X-Explicit"));
    assertTrue(installedPlugins.pluginProblems().isEmpty());
  }

  private void writePluginJar(Path jarPath) throws Exception {
    String providerClassName = "cafe.woden.ircclient.testplugins.PluginSharedPreviewHeaders";
    String providerSource =
        pluginProviderSource("cafe.woden.ircclient.ui.chat.embed.spi.EmbedHttpHeaderProvider");
    CompiledPluginJarSupport.writePluginJar(
        jarPath,
        providerClassName,
        providerSource,
        cafe.woden.ircclient.ui.chat.embed.spi.EmbedHttpHeaderProvider.class.getName(),
        CompiledPluginJarSupport.compatibleManifest("plugin-shared-embed-preview-header", "1.0.0"));
  }

  private static String pluginProviderSource(String providerImport) {
    return """
        package cafe.woden.ircclient.testplugins;

        import %s;
        import java.net.URI;
        import java.util.Map;

        public final class PluginSharedPreviewHeaders implements EmbedHttpHeaderProvider {
          @Override
          public Map<String, String> embedHttpHeaders(URI uri) {
            if (uri == null || !\"cards.example.test\".equals(uri.getHost())) {
              return Map.of();
            }
            return Map.of(
                \"Referer\", \"https://cards.example.test/\",
                \"X-Embed-Plugin\", \"IRCafe Shared Preview Plugin\");
          }
        }
        """
        .formatted(providerImport);
  }
}
