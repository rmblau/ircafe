package cafe.woden.ircclient.ui.chat.embed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.config.api.RuntimeConfigPathPort;
import cafe.woden.ircclient.config.plugins.InstalledPluginServices;
import cafe.woden.ircclient.ui.chat.embed.spi.NewsPublisherProfile;
import cafe.woden.ircclient.ui.chat.embed.spi.NewsPublisherProfileProvider;
import cafe.woden.ircclient.util.CompiledPluginJarSupport;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class NewsPublisherProfileProviderPluginTest {

  @TempDir Path tempDir;

  @Test
  void loadsNewsPublisherProfilesFromPluginDirectoryJar() throws Exception {
    Path runtimeConfigDirectory = Files.createDirectories(tempDir.resolve("config-home/ircafe"));
    Path pluginDir = Files.createDirectories(runtimeConfigDirectory.resolve("plugins"));
    writePluginJar(pluginDir.resolve("plugin-news-publisher.jar"));
    RuntimeConfigPathPort runtimeConfigPathPort =
        () -> runtimeConfigDirectory.resolve("ircafe.yml");
    InstalledPluginServices installedPlugins = new InstalledPluginServices(runtimeConfigPathPort);

    List<NewsPublisherProfile> profiles =
        LinkPreviewPluginProviders.newsPublisherProfiles(installedPlugins);

    String url = "https://pluginpublisher.example/story/plugin-article-123456";
    assertTrue(NewsPreviewUtil.isLikelyNewsArticleUri(URI.create(url), profiles));

    var doc =
        Jsoup.parse(
            """
            <html>
              <head>
                <title>Plugin Article - Plugin Daily</title>
                <meta name='plugin-author' content='Plugin Reporter'>
                <meta name='plugin-date' content='2026-06-13'>
                <meta property='og:image' content='https://cdn.example/plugin-news.png'>
              </head>
              <body>
                <main data-plugin-article>
                  <p>This plugin article opening paragraph is long enough to look like useful article body copy.</p>
                  <p>The plugin article second paragraph verifies that contributed selectors drive summary extraction.</p>
                </main>
              </body>
            </html>
            """,
            url);

    LinkPreview preview = NewsPreviewUtil.parseArticleDocument(doc, url, profiles);

    assertNotNull(preview);
    assertEquals("Plugin Article", preview.title());
    assertEquals("Plugin Daily", preview.siteName());
    assertEquals("https://cdn.example/plugin-news.png", preview.imageUrl());
    assertTrue(preview.description().contains("Author: Plugin Reporter"));
    assertTrue(preview.description().contains("Date: 2026-06-13"));
    assertTrue(preview.description().contains("Publisher: Plugin Daily"));
    assertTrue(preview.description().contains("contributed selectors drive summary extraction"));
    assertTrue(installedPlugins.pluginProblems().isEmpty());
  }

  private void writePluginJar(Path jarPath) throws Exception {
    String providerClassName = "cafe.woden.ircclient.testplugins.PluginNewsPublisherProvider";
    String providerSource =
        """
        package cafe.woden.ircclient.testplugins;

        import cafe.woden.ircclient.ui.chat.embed.spi.NewsPublisherProfile;
        import cafe.woden.ircclient.ui.chat.embed.spi.NewsPublisherProfileProvider;
        import java.util.List;

        public final class PluginNewsPublisherProvider implements NewsPublisherProfileProvider {
          @Override
          public List<NewsPublisherProfile> publisherProfiles() {
            return List.of(
                new NewsPublisherProfile(
                    "plugin-daily",
                    "Plugin Daily",
                    new String[] {"pluginpublisher.example"},
                    new String[] {"main[data-plugin-article] p"},
                    new String[] {"meta[name='plugin-author']"},
                    new String[] {"meta[property='og:image']"},
                    new String[] {"plugin-author"},
                    new String[] {"plugin-date"}));
          }
        }
        """;
    CompiledPluginJarSupport.writePluginJar(
        jarPath,
        providerClassName,
        providerSource,
        NewsPublisherProfileProvider.class.getName(),
        CompiledPluginJarSupport.compatibleManifest("plugin-news-publisher", "1.0.0"));
  }
}
