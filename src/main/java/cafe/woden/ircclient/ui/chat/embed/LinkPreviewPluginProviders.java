package cafe.woden.ircclient.ui.chat.embed;

import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.ui.chat.embed.spi.NewsPublisherProfile;
import cafe.woden.ircclient.ui.chat.embed.spi.NewsPublisherProfileProvider;
import cafe.woden.ircclient.ui.chat.embed.spi.OEmbedLinkPreviewProvider;
import java.util.List;
import org.jmolecules.architecture.layered.InterfaceLayer;

/** Shared helpers for ServiceLoader-backed link preview plugin providers. */
@InterfaceLayer
final class LinkPreviewPluginProviders {
  private LinkPreviewPluginProviders() {}

  static List<LinkPreviewResolver> linkPreviewResolvers(
      List<LinkPreviewResolver> builtInResolvers, InstalledPluginsPort installedPlugins) {
    List<LinkPreviewResolver> resolvers =
        List.copyOf(builtInResolvers == null ? List.of() : builtInResolvers);
    if (installedPlugins == null) {
      return resolvers;
    }
    return installedPlugins.loadInstalledServices(LinkPreviewResolver.class, resolvers);
  }

  static List<OEmbedLinkPreviewProvider> oEmbedProviders(InstalledPluginsPort installedPlugins) {
    List<OEmbedLinkPreviewProvider> providers = OEmbedLinkPreviewResolver.defaultProviders();
    if (installedPlugins == null) {
      return providers;
    }
    return installedPlugins.loadInstalledServices(OEmbedLinkPreviewProvider.class, providers);
  }

  static List<NewsPublisherProfile> newsPublisherProfiles(InstalledPluginsPort installedPlugins) {
    if (installedPlugins == null) {
      return NewsPreviewUtil.publisherProfilesFromProviders(List.of());
    }
    List<NewsPublisherProfileProvider> providers =
        installedPlugins.loadInstalledServices(NewsPublisherProfileProvider.class, List.of());
    return NewsPreviewUtil.publisherProfilesFromProviders(providers);
  }
}
