package cafe.woden.ircclient.ui.chat.embed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.config.api.RuntimeConfigPathPort;
import cafe.woden.ircclient.config.plugins.InstalledPluginServices;
import cafe.woden.ircclient.util.CompiledPluginJarSupport;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LinkUrlExtractorImageUrlExtensionProviderPluginTest {

  @TempDir Path tempDir;

  @Test
  void excludesPluginImageExtensionsFromLinkPreviewExtraction() throws Exception {
    Path runtimeConfigDirectory = Files.createDirectories(tempDir.resolve("config-home/ircafe"));
    Path pluginDir = Files.createDirectories(runtimeConfigDirectory.resolve("plugins"));
    writePluginJar(pluginDir.resolve("plugin-image-url-extension.jar"));
    RuntimeConfigPathPort runtimeConfigPathPort =
        () -> runtimeConfigDirectory.resolve("ircafe.yml");
    InstalledPluginServices installedPlugins = new InstalledPluginServices(runtimeConfigPathPort);

    List<cafe.woden.ircclient.ui.chat.embed.spi.ImageUrlExtensionProvider> providers =
        ImageUrlExtensionProviders.loadInstalledProviders(installedPlugins);
    List<String> urls =
        LinkUrlExtractor.extractUrls(
            "image https://cdn.example/artwork.jxl page https://example.com/post", providers);

    assertFalse(providers.isEmpty());
    assertEquals(List.of("https://example.com/post"), urls);
    assertTrue(installedPlugins.pluginProblems().isEmpty());
  }

  private void writePluginJar(Path jarPath) throws Exception {
    String providerClassName = "cafe.woden.ircclient.testplugins.PluginImageUrlExtension";
    String providerSource =
        pluginProviderSource("cafe.woden.ircclient.ui.chat.embed.spi.ImageUrlExtensionProvider");
    CompiledPluginJarSupport.writePluginJar(
        jarPath,
        providerClassName,
        providerSource,
        cafe.woden.ircclient.ui.chat.embed.spi.ImageUrlExtensionProvider.class.getName(),
        CompiledPluginJarSupport.compatibleManifest("plugin-image-url-extension", "1.0.0"));
  }

  private static String pluginProviderSource(String providerImport) {
    return """
        package cafe.woden.ircclient.testplugins;

        import %s;
        import java.util.List;

        public final class PluginImageUrlExtension implements ImageUrlExtensionProvider {
          @Override
          public List<String> imageFileExtensions() {
            return List.of("jxl");
          }
        }
        """
        .formatted(providerImport);
  }
}
