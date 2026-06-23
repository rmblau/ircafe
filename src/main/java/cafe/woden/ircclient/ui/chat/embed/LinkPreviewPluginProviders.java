package cafe.woden.ircclient.ui.chat.embed;

import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreviewResolver;
import cafe.woden.ircclient.ui.chat.embed.spi.NewsPublisherProfile;
import cafe.woden.ircclient.ui.chat.embed.spi.NewsPublisherProfileProvider;
import cafe.woden.ircclient.ui.chat.embed.spi.OEmbedLinkPreviewProvider;
import cafe.woden.ircclient.util.PluginServiceLoaderSupport;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
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
    List<OEmbedLinkPreviewProvider> builtInProviders = builtInOEmbedProviders();
    if (installedPlugins == null) {
      return builtInProviders;
    }
    return dedupeByProviderClass(
        installedPlugins.loadInstalledServices(OEmbedLinkPreviewProvider.class, builtInProviders));
  }

  static List<NewsPublisherProfile> newsPublisherProfiles(InstalledPluginsPort installedPlugins) {
    List<NewsPublisherProfileProvider> providers = builtInNewsProfileProviders();
    if (installedPlugins != null) {
      providers =
          dedupeByProviderClass(
              installedPlugins.loadInstalledServices(
                  NewsPublisherProfileProvider.class, providers));
    }
    return NewsPreviewUtil.publisherProfilesFromProviders(providers);
  }

  private static List<OEmbedLinkPreviewProvider> builtInOEmbedProviders() {
    return PluginServiceLoaderSupport.loadInstalledServices(
        OEmbedLinkPreviewProvider.class,
        List.of(),
        PluginServiceLoaderSupport.defaultApplicationClassLoader(LinkPreviewPluginProviders.class),
        null);
  }

  private static List<NewsPublisherProfileProvider> builtInNewsProfileProviders() {
    return PluginServiceLoaderSupport.loadInstalledServices(
        NewsPublisherProfileProvider.class,
        List.of(),
        PluginServiceLoaderSupport.defaultApplicationClassLoader(LinkPreviewPluginProviders.class),
        null);
  }

  private static <T> List<T> dedupeByProviderClass(List<? extends T> services) {
    LinkedHashSet<String> providerClassNames = new LinkedHashSet<>();
    ArrayList<T> deduped = new ArrayList<>();
    for (T service : Objects.requireNonNullElse(services, List.<T>of())) {
      if (service == null || !providerClassNames.add(service.getClass().getName())) {
        continue;
      }
      deduped.add(service);
    }
    return List.copyOf(deduped);
  }
}
