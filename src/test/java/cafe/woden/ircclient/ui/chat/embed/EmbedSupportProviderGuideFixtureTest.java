package cafe.woden.ircclient.ui.chat.embed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.config.api.RuntimeConfigPathPort;
import cafe.woden.ircclient.config.plugins.InstalledPluginServices;
import cafe.woden.ircclient.config.plugins.InstalledPluginServicesTestSupport;
import cafe.woden.ircclient.ui.chat.embed.spi.EmbedHttpHeaderProvider;
import cafe.woden.ircclient.ui.chat.embed.spi.ImageUrlExtensionProvider;
import cafe.woden.ircclient.util.CompiledPluginJarSupport;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class EmbedSupportProviderGuideFixtureTest {

  private static final String GUIDE_IMAGE_EXTENSION_PROVIDER_CLASS =
      "example.embed.ExampleImageUrlExtensionProvider";
  private static final String GUIDE_HEADER_PROVIDER_CLASS =
      "example.embed.ExampleEmbedHttpHeaderProvider";

  @TempDir Path tempDir;

  @Test
  void documentedEmbedSupportPluginCanPackageImageExtensionAndHeaderProviders() throws Exception {
    Path runtimeConfigDirectory = Files.createDirectories(tempDir.resolve("config-home/ircafe"));
    Path pluginDir = Files.createDirectories(runtimeConfigDirectory.resolve("plugins"));
    CompiledPluginJarSupport.writePluginJar(
        pluginDir.resolve("embed-support-guide-example.jar"),
        Map.of(
            GUIDE_IMAGE_EXTENSION_PROVIDER_CLASS, guideImageExtensionProviderSource(),
            GUIDE_HEADER_PROVIDER_CLASS, guideHeaderProviderSource()),
        Map.of(
            ImageUrlExtensionProvider.class.getName(),
            List.of(GUIDE_IMAGE_EXTENSION_PROVIDER_CLASS),
            EmbedHttpHeaderProvider.class.getName(),
            List.of(GUIDE_HEADER_PROVIDER_CLASS)),
        CompiledPluginJarSupport.compatibleManifest("embed-support-guide-example", "1.0.0"));
    RuntimeConfigPathPort runtimeConfigPathPort =
        () -> runtimeConfigDirectory.resolve("ircafe.yml");

    InstalledPluginServices installedPlugins = new InstalledPluginServices(runtimeConfigPathPort);
    try {
      List<ImageUrlExtensionProvider> extensionProviders =
          ImageUrlExtensionProviders.loadInstalledProviders(installedPlugins);
      List<EmbedHttpHeaderProvider> headerProviders =
          EmbedHttpHeaderProviders.loadInstalledProviders(installedPlugins);
      Map<String, String> headers =
          PreviewHttp.headersFor(
              URI.create("https://cdn.example.test/artwork.jxl"),
              "image/jxl",
              Map.of(),
              headerProviders);

      assertTrue(installedPlugins.pluginProblems().isEmpty());
      assertEquals(
          ".jxl",
          ImageFileExtensionSupport.extensionFromUrl(
              "https://cdn.example.test/artwork.JXL?download=1", extensionProviders));
      assertEquals("IRCafe Embed Guide", headers.get("X-Embed-Plugin"));
      assertEquals("https://cdn.example.test/", headers.get(PreviewHttp.HEADER_REFERER));
    } finally {
      InstalledPluginServicesTestSupport.shutdown(installedPlugins);
    }
  }

  private static String guideImageExtensionProviderSource() {
    return """
        package example.embed;

        import cafe.woden.ircclient.ui.chat.embed.spi.ImageUrlExtensionProvider;
        import java.util.List;

        public final class ExampleImageUrlExtensionProvider implements ImageUrlExtensionProvider {
          @Override
          public List<String> imageFileExtensions() {
            return List.of(".jxl");
          }
        }
        """;
  }

  private static String guideHeaderProviderSource() {
    return """
        package example.embed;

        import cafe.woden.ircclient.ui.chat.embed.spi.EmbedHttpHeaderProvider;
        import java.net.URI;
        import java.util.Map;

        public final class ExampleEmbedHttpHeaderProvider implements EmbedHttpHeaderProvider {
          @Override
          public Map<String, String> embedHttpHeaders(URI uri) {
            if (uri == null || !"cdn.example.test".equals(uri.getHost())) {
              return Map.of();
            }
            return Map.of(
                "Referer", "https://cdn.example.test/",
                "X-Embed-Plugin", "IRCafe Embed Guide");
          }
        }
        """;
  }
}
