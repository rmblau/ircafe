package cafe.woden.ircclient.ui.chat.embed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.config.api.RuntimeConfigPathPort;
import cafe.woden.ircclient.config.plugins.InstalledPluginServices;
import cafe.woden.ircclient.util.CompiledPluginJarSupport;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LinkPreviewFetchServiceTest {

  @TempDir Path tempDir;

  @Test
  void loadsResolversFromInstalledPluginsPort() {
    LinkPreviewFetchService service =
        new LinkPreviewFetchService(
            null,
            List.of((uri, originalUrl, http) -> null),
            new FakeInstalledPluginsPort(List.of(new PluginLinkPreviewResolver())));

    LinkPreview preview = service.fetch("server-a", "https://plugin.example/item").blockingGet();

    assertEquals("Plugin preview", preview.title());
    assertEquals("Plugin", preview.siteName());
  }

  @Test
  void loadsResolversFromPluginDirectoryJar() throws Exception {
    Path runtimeConfigDirectory = Files.createDirectories(tempDir.resolve("config-home/ircafe"));
    Path pluginDir = Files.createDirectories(runtimeConfigDirectory.resolve("plugins"));
    writePluginJar(pluginDir.resolve("plugin-link-preview.jar"));
    RuntimeConfigPathPort runtimeConfigPathPort =
        () -> runtimeConfigDirectory.resolve("ircafe.yml");
    InstalledPluginServices installedPlugins = new InstalledPluginServices(runtimeConfigPathPort);

    LinkPreviewFetchService service =
        new LinkPreviewFetchService(null, List.of(), installedPlugins);

    LinkPreview preview = service.fetch("server-a", "https://plugin.example/item").blockingGet();

    assertEquals("Plugin preview", preview.title());
    assertEquals("Plugin", preview.siteName());
    assertTrue(installedPlugins.pluginProblems().isEmpty());
  }

  private void writePluginJar(Path jarPath) throws Exception {
    String providerClassName = "cafe.woden.ircclient.testplugins.PluginLinkPreviewResolver";
    String providerSource =
        """
        package cafe.woden.ircclient.testplugins;

        import cafe.woden.ircclient.ui.chat.embed.LinkPreview;
        import cafe.woden.ircclient.ui.chat.embed.LinkPreviewResolver;
        import cafe.woden.ircclient.ui.chat.embed.PreviewHttp;
        import java.net.URI;

        public final class PluginLinkPreviewResolver implements LinkPreviewResolver {
          public LinkPreview tryResolve(URI uri, String originalUrl, PreviewHttp http) {
            if (!"plugin.example".equals(uri.getHost())) return null;
            return new LinkPreview(
                originalUrl, "Plugin preview", "from plugin jar", "Plugin", null, 0);
          }
        }
        """;
    CompiledPluginJarSupport.writePluginJar(
        jarPath,
        providerClassName,
        providerSource,
        LinkPreviewResolver.class.getName(),
        CompiledPluginJarSupport.compatibleManifest("plugin-link-preview", "1.0.0"));
  }

  private static final class FakeInstalledPluginsPort implements InstalledPluginsPort {
    private final List<?> pluginServices;

    private FakeInstalledPluginsPort(List<?> pluginServices) {
      this.pluginServices = List.copyOf(pluginServices);
    }

    @Override
    public <T> List<T> loadInstalledServices(Class<T> serviceType, List<T> builtInServices) {
      java.util.ArrayList<T> services =
          new java.util.ArrayList<>(
              java.util.Objects.requireNonNullElse(builtInServices, List.of()));
      for (Object pluginService : pluginServices) {
        if (serviceType.isInstance(pluginService)) {
          services.add(serviceType.cast(pluginService));
        }
      }
      return List.copyOf(services);
    }
  }

  private static final class PluginLinkPreviewResolver implements LinkPreviewResolver {
    @Override
    public LinkPreview tryResolve(URI uri, String originalUrl, PreviewHttp http) {
      if (!"plugin.example".equals(uri.getHost())) {
        return null;
      }
      return new LinkPreview(originalUrl, "Plugin preview", "from fake port", "Plugin", null, 0);
    }
  }
}
