package cafe.woden.ircclient.ui.chat.embed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.config.api.RuntimeConfigPathPort;
import cafe.woden.ircclient.config.plugins.InstalledPluginServices;
import cafe.woden.ircclient.config.plugins.InstalledPluginServicesTestSupport;
import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreview;
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

class NewsPublisherProfileProviderGuideFixtureTest {

  private static final String GUIDE_PROVIDER_CLASS =
      "example.embed.ExampleNewsPublisherProfileProvider";

  @TempDir Path tempDir;

  @Test
  void documentedNewsPublisherProfileProviderContributesArticleExtractionSelectors()
      throws Exception {
    Path runtimeConfigDirectory = Files.createDirectories(tempDir.resolve("config-home/ircafe"));
    Path pluginDir = Files.createDirectories(runtimeConfigDirectory.resolve("plugins"));
    CompiledPluginJarSupport.writePluginJar(
        pluginDir.resolve("news-publisher-profile-guide-example.jar"),
        GUIDE_PROVIDER_CLASS,
        guideProviderSource(),
        NewsPublisherProfileProvider.class.getName(),
        CompiledPluginJarSupport.compatibleManifest(
            "news-publisher-profile-guide-example", "1.0.0"));
    RuntimeConfigPathPort runtimeConfigPathPort =
        () -> runtimeConfigDirectory.resolve("ircafe.yml");

    InstalledPluginServices installedPlugins = new InstalledPluginServices(runtimeConfigPathPort);
    try {
      List<NewsPublisherProfile> profiles =
          LinkPreviewPluginProviders.newsPublisherProfiles(installedPlugins);
      String url = "https://guide-news.example/world/us/example-story-123456";
      var doc =
          Jsoup.parse(
              """
              <html>
                <head>
                  <title>Guide Article - Guide Daily</title>
                  <meta name='guide-author' content='Guide Reporter'>
                  <meta name='guide-date' content='2026-06-26'>
                  <meta property='og:image' content='https://cdn.example/guide-news.png'>
                </head>
                <body>
                  <article data-guide-story>
                    <p>The guide article opening paragraph has enough useful words for summary extraction.</p>
                    <p>The second guide paragraph proves plugin-provided selectors drive article previews.</p>
                  </article>
                </body>
              </html>
              """,
              url);

      assertTrue(installedPlugins.pluginProblems().isEmpty());
      assertTrue(NewsPreviewUtil.isLikelyNewsArticleUri(URI.create(url), profiles));

      LinkPreview preview = NewsPreviewUtil.parseArticleDocument(doc, url, profiles);

      assertNotNull(preview);
      assertEquals("Guide Article", preview.title());
      assertEquals("Guide Daily", preview.siteName());
      assertEquals("https://cdn.example/guide-news.png", preview.imageUrl());
      assertTrue(preview.description().contains("Author: Guide Reporter"));
      assertTrue(preview.description().contains("Date: 2026-06-26"));
      assertTrue(
          preview.description().contains("plugin-provided selectors drive article previews"));
    } finally {
      InstalledPluginServicesTestSupport.shutdown(installedPlugins);
    }
  }

  private static String guideProviderSource() {
    return """
        package example.embed;

        import cafe.woden.ircclient.ui.chat.embed.spi.NewsPublisherProfile;
        import cafe.woden.ircclient.ui.chat.embed.spi.NewsPublisherProfileProvider;
        import java.util.List;

        public final class ExampleNewsPublisherProfileProvider
            implements NewsPublisherProfileProvider {
          @Override
          public List<NewsPublisherProfile> publisherProfiles() {
            return List.of(
                new NewsPublisherProfile(
                    "guide-daily",
                    "Guide Daily",
                    new String[] {"www.guide-news.example", "guide-news.example"},
                    new String[] {"article[data-guide-story] p"},
                    new String[] {"meta[name='guide-author']"},
                    new String[] {"meta[property='og:image']"},
                    new String[] {"guide-author"},
                    new String[] {"guide-date"}));
          }
        }
        """;
  }
}
