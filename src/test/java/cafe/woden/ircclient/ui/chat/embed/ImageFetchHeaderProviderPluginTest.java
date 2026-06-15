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

/** Compatibility coverage for the deprecated image-specific header-provider SPI. */
class ImageFetchHeaderProviderPluginTest {

  @TempDir Path tempDir;

  @Test
  void loadsImageFetchHeadersFromPluginDirectoryJar() throws Exception {
    Path runtimeConfigDirectory = Files.createDirectories(tempDir.resolve("config-home/ircafe"));
    Path pluginDir = Files.createDirectories(runtimeConfigDirectory.resolve("plugins"));
    writePluginJar(pluginDir.resolve("plugin-image-fetch-header.jar"));
    RuntimeConfigPathPort runtimeConfigPathPort =
        () -> runtimeConfigDirectory.resolve("ircafe.yml");
    InstalledPluginServices installedPlugins = new InstalledPluginServices(runtimeConfigPathPort);

    List<ImageFetchHeaderProvider> providers =
        installedPlugins.loadInstalledServices(ImageFetchHeaderProvider.class, List.of());
    Map<String, String> headers =
        ImageFetchService.headersFor(URI.create("https://cdn.example.test/poster.png"), providers);

    assertEquals("https://cdn.example.test/", headers.get(PreviewHttp.HEADER_REFERER));
    assertEquals("IRCafe Plugin", headers.get("X-Image-Plugin"));
    assertTrue(installedPlugins.pluginProblems().isEmpty());
  }

  private void writePluginJar(Path jarPath) throws Exception {
    String providerClassName = "cafe.woden.ircclient.testplugins.PluginImageFetchHeaders";
    String providerSource =
        """
        package cafe.woden.ircclient.testplugins;

        import cafe.woden.ircclient.ui.chat.embed.ImageFetchHeaderProvider;
        import java.net.URI;
        import java.util.Map;

        public final class PluginImageFetchHeaders implements ImageFetchHeaderProvider {
          @Override
          public Map<String, String> imageFetchHeaders(URI imageUri) {
            if (imageUri == null || !"cdn.example.test".equals(imageUri.getHost())) {
              return Map.of();
            }
            return Map.of(
                "Referer", "https://cdn.example.test/",
                "X-Image-Plugin", "IRCafe Plugin");
          }
        }
        """;
    CompiledPluginJarSupport.writePluginJar(
        jarPath,
        providerClassName,
        providerSource,
        ImageFetchHeaderProvider.class.getName(),
        CompiledPluginJarSupport.compatibleManifest("plugin-image-fetch-header", "1.0.0"));
  }
}
