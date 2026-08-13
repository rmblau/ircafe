package cafe.woden.ircclient.ui.chat.embed;

import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreviewResolver;
import org.jmolecules.architecture.layered.InterfaceLayer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * Spring wiring for stateful/configured link preview resolvers.
 *
 * <p>No-arg resolvers are contributed through ServiceLoader and ordered by {@link
 * LinkPreviewResolver#sortOrder()}.
 */
@Configuration
@InterfaceLayer
public class LinkPreviewResolverConfig {

  // Keep in sync with any resolver HTML caps.
  static final int DEFAULT_MAX_HTML_BYTES = 1024 * 1024; // 1 MiB

  @Bean
  @Order(1)
  LinkPreviewResolver wikipediaLinkPreviewResolver() {
    return new WikipediaLinkPreviewResolver();
  }

  @Bean
  @Order(2)
  LinkPreviewResolver youTubeLinkPreviewResolver() {
    return new YouTubeLinkPreviewResolver();
  }

  @Bean
  @Order(3)
  LinkPreviewResolver slashdotLinkPreviewResolver() {
    return new SlashdotLinkPreviewResolver(DEFAULT_MAX_HTML_BYTES);
  }

  @Bean
  @Order(4)
  LinkPreviewResolver imdbLinkPreviewResolver() {
    return new ImdbLinkPreviewResolver();
  }

  @Bean
  @Order(5)
  LinkPreviewResolver rottenTomatoesLinkPreviewResolver() {
    return new RottenTomatoesLinkPreviewResolver();
  }

  @Bean
  @Order(6)
  LinkPreviewResolver xLinkPreviewResolver() {
    return new XLinkPreviewResolver(DEFAULT_MAX_HTML_BYTES);
  }

  @Bean
  @Order(7)
  LinkPreviewResolver instagramLinkPreviewResolver() {
    return new InstagramLinkPreviewResolver(DEFAULT_MAX_HTML_BYTES);
  }

  @Bean
  @Order(8)
  LinkPreviewResolver imgurLinkPreviewResolver() {
    return new ImgurLinkPreviewResolver(DEFAULT_MAX_HTML_BYTES);
  }

  @Bean
  @Order(9)
  LinkPreviewResolver gitHubLinkPreviewResolver() {
    return new GitHubLinkPreviewResolver();
  }

  @Bean
  @Order(10)
  LinkPreviewResolver redditLinkPreviewResolver() {
    return new RedditLinkPreviewResolver();
  }

  @Bean
  @Order(11)
  LinkPreviewResolver mastodonStatusApiPreviewResolver() {
    return new MastodonStatusApiPreviewResolver();
  }

  @Bean
  @Order(12)
  LinkPreviewResolver oEmbedLinkPreviewResolver(
      ObjectProvider<InstalledPluginsPort> installedPluginsProvider) {
    return oEmbedLinkPreviewResolver(resolveInstalledPlugins(installedPluginsProvider));
  }

  static LinkPreviewResolver oEmbedLinkPreviewResolver(InstalledPluginsPort installedPlugins) {
    return new OEmbedLinkPreviewResolver(
        LinkPreviewPluginProviders.oEmbedProviders(installedPlugins));
  }

  private static InstalledPluginsPort resolveInstalledPlugins(
      ObjectProvider<InstalledPluginsPort> installedPluginsProvider) {
    return installedPluginsProvider == null ? null : installedPluginsProvider.getIfAvailable();
  }

  @Bean
  @Order(13)
  LinkPreviewResolver newsLinkPreviewResolver(
      ObjectProvider<InstalledPluginsPort> installedPluginsProvider) {
    return newsLinkPreviewResolver(resolveInstalledPlugins(installedPluginsProvider));
  }

  static LinkPreviewResolver newsLinkPreviewResolver(InstalledPluginsPort installedPlugins) {
    return new NewsLinkPreviewResolver(
        DEFAULT_MAX_HTML_BYTES, LinkPreviewPluginProviders.newsPublisherProfiles(installedPlugins));
  }

  @Bean
  @Order(14)
  LinkPreviewResolver openGraphLinkPreviewResolver() {
    return new OpenGraphLinkPreviewResolver(DEFAULT_MAX_HTML_BYTES);
  }
}
