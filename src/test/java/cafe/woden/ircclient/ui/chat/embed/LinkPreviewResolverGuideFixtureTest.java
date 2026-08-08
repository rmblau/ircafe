package cafe.woden.ircclient.ui.chat.embed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.config.api.RuntimeConfigPathPort;
import cafe.woden.ircclient.config.plugins.InstalledPluginServices;
import cafe.woden.ircclient.config.plugins.InstalledPluginServicesTestSupport;
import cafe.woden.ircclient.ui.chat.embed.spi.BuiltInLinkPreviewResolverOrders;
import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreview;
import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreviewResolver;
import cafe.woden.ircclient.util.CompiledPluginJarSupport;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LinkPreviewResolverGuideFixtureTest {

  private static final String GUIDE_PROVIDER_CLASS = "example.preview.ExampleLinkPreviewResolver";

  @TempDir Path tempDir;

  @Test
  void documentedExampleResolverLoadsThroughInstalledPluginCatalog() throws Exception {
    Path runtimeConfigDirectory = Files.createDirectories(tempDir.resolve("config-home/ircafe"));
    Path pluginDir = Files.createDirectories(runtimeConfigDirectory.resolve("plugins"));
    CompiledPluginJarSupport.writePluginJar(
        pluginDir.resolve("link-preview-guide-example.jar"),
        GUIDE_PROVIDER_CLASS,
        guideProviderSource(),
        LinkPreviewResolver.class.getName(),
        CompiledPluginJarSupport.compatibleManifest("link-preview-guide-example", "1.0.0"));
    RuntimeConfigPathPort runtimeConfigPathPort =
        () -> runtimeConfigDirectory.resolve("ircafe.yml");

    InstalledPluginServices installedPlugins = new InstalledPluginServices(runtimeConfigPathPort);
    try {
      List<LinkPreviewResolver> resolvers =
          LinkPreviewPluginProviders.linkPreviewResolvers(List.of(), installedPlugins);

      assertTrue(installedPlugins.pluginProblems().isEmpty());
      assertTrue(
          resolvers.stream()
              .anyMatch(resolver -> GUIDE_PROVIDER_CLASS.equals(resolver.getClass().getName())));
      assertTrue(
          resolvers.stream()
              .filter(resolver -> GUIDE_PROVIDER_CLASS.equals(resolver.getClass().getName()))
              .allMatch(
                  resolver ->
                      resolver.sortOrder()
                          == BuiltInLinkPreviewResolverOrders.PLUGIN_DEFAULT + 50));

      LinkPreviewFetchService service =
          new LinkPreviewFetchService(null, List.of(), installedPlugins);
      LinkPreview preview =
          service.fetch("server-a", "https://guide-preview.example/items/42").blockingGet();

      assertEquals("Guide preview", preview.title());
      assertEquals("Plugin Guide", preview.siteName());
      assertEquals("Resolved by an external LinkPreviewResolver plugin.", preview.description());
      assertEquals("https://guide-preview.example/items/42", preview.url());
    } finally {
      InstalledPluginServicesTestSupport.shutdown(installedPlugins);
    }
  }

  private static String guideProviderSource() {
    return """
        package example.preview;

        import cafe.woden.ircclient.ui.chat.embed.spi.BuiltInLinkPreviewResolverOrders;
        import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreview;
        import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreviewHttp;
        import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreviewResolver;
        import java.net.URI;

        public final class ExampleLinkPreviewResolver implements LinkPreviewResolver {
          @Override
          public int sortOrder() {
            return BuiltInLinkPreviewResolverOrders.PLUGIN_DEFAULT + 50;
          }

          @Override
          public LinkPreview tryResolve(URI uri, String originalUrl, LinkPreviewHttp http) {
            if (uri == null || !"guide-preview.example".equals(uri.getHost())) {
              return null;
            }
            return new LinkPreview(
                originalUrl,
                "Guide preview",
                "Resolved by an external LinkPreviewResolver plugin.",
                "Plugin Guide",
                null,
                0);
          }
        }
        """;
  }
}
