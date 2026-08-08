package cafe.woden.ircclient.ui.chat.embed;

import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreviewResolver;
import cafe.woden.ircclient.ui.chat.embed.spi.NewsPublisherProfile;
import cafe.woden.ircclient.ui.chat.embed.spi.NewsPublisherProfileProvider;
import cafe.woden.ircclient.ui.chat.embed.spi.OEmbedLinkPreviewProvider;
import cafe.woden.ircclient.util.PluginServiceLoaderSupport;
import java.util.List;
import org.jmolecules.architecture.layered.InterfaceLayer;

/** Root bridge for ServiceLoader-backed link-preview plugin providers. */
@InterfaceLayer
final class LinkPreviewPluginProviders {
  private static final LinkPreviewProviderCatalog CATALOG = new LinkPreviewProviderCatalog();

  private LinkPreviewPluginProviders() {}

  static List<LinkPreviewResolver> linkPreviewResolvers(
      List<LinkPreviewResolver> builtInResolvers, InstalledPluginsPort installedPlugins) {
    List<LinkPreviewResolver> applicationResolvers =
        PluginServiceLoaderSupport.loadApplicationServices(
            LinkPreviewResolver.class, builtInResolvers, LinkPreviewPluginProviders.class);
    List<LinkPreviewResolver> installedResolvers =
        installedPlugins == null
            ? List.of()
            : installedPlugins.loadInstalledServices(
                LinkPreviewResolver.class, applicationResolvers);
    return CATALOG.linkPreviewResolvers(applicationResolvers, installedResolvers);
  }

  static List<OEmbedLinkPreviewProvider> oEmbedProviders(InstalledPluginsPort installedPlugins) {
    List<OEmbedLinkPreviewProvider> builtInProviders = builtInOEmbedProviders();
    List<OEmbedLinkPreviewProvider> installedProviders =
        installedPlugins == null
            ? List.of()
            : installedPlugins.loadInstalledServices(
                OEmbedLinkPreviewProvider.class, builtInProviders);
    return CATALOG.oEmbedProviders(builtInProviders, installedProviders);
  }

  static List<NewsPublisherProfile> newsPublisherProfiles(InstalledPluginsPort installedPlugins) {
    List<NewsPublisherProfileProvider> builtInProviders = builtInNewsProfileProviders();
    List<NewsPublisherProfileProvider> installedProviders =
        installedPlugins == null
            ? List.of()
            : installedPlugins.loadInstalledServices(
                NewsPublisherProfileProvider.class, builtInProviders);
    return CATALOG.newsPublisherProfiles(
        CATALOG.newsPublisherProfileProviders(builtInProviders, installedProviders));
  }

  private static List<OEmbedLinkPreviewProvider> builtInOEmbedProviders() {
    return CATALOG.oEmbedProviders(
        PluginServiceLoaderSupport.loadApplicationServices(
            OEmbedLinkPreviewProvider.class, LinkPreviewPluginProviders.class),
        List.of());
  }

  private static List<NewsPublisherProfileProvider> builtInNewsProfileProviders() {
    return CATALOG.newsPublisherProfileProviders(
        PluginServiceLoaderSupport.loadApplicationServices(
            NewsPublisherProfileProvider.class, LinkPreviewPluginProviders.class),
        List.of());
  }
}
