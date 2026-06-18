package cafe.woden.ircclient.ui.chat.embed;

import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.ui.chat.embed.builtins.BuiltInMastodonOEmbedLinkPreviewProvider;
import cafe.woden.ircclient.ui.chat.embed.builtins.BuiltInNewsPublisherProfileProvider;
import cafe.woden.ircclient.ui.chat.embed.builtins.BuiltInSpotifyOEmbedLinkPreviewProvider;
import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreviewResolver;
import cafe.woden.ircclient.ui.chat.embed.spi.NewsPublisherProfile;
import cafe.woden.ircclient.ui.chat.embed.spi.NewsPublisherProfileProvider;
import cafe.woden.ircclient.ui.chat.embed.spi.OEmbedLinkPreviewProvider;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import org.jmolecules.architecture.layered.InterfaceLayer;

/** Shared helpers for ServiceLoader-backed link preview plugin providers. */
@InterfaceLayer
final class LinkPreviewPluginProviders {
  private static final List<NewsPublisherProfileProvider> BUILT_IN_NEWS_PROFILE_PROVIDERS =
      List.of(new BuiltInNewsPublisherProfileProvider());
  private static final List<OEmbedLinkPreviewProvider> BUILT_IN_OEMBED_PROVIDERS =
      List.of(
          new BuiltInSpotifyOEmbedLinkPreviewProvider(),
          new BuiltInMastodonOEmbedLinkPreviewProvider());

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
    if (installedPlugins == null) {
      return BUILT_IN_OEMBED_PROVIDERS;
    }
    return dedupeByProviderClass(
        installedPlugins.loadInstalledServices(
            OEmbedLinkPreviewProvider.class, BUILT_IN_OEMBED_PROVIDERS));
  }

  static List<NewsPublisherProfile> newsPublisherProfiles(InstalledPluginsPort installedPlugins) {
    List<NewsPublisherProfileProvider> providers = BUILT_IN_NEWS_PROFILE_PROVIDERS;
    if (installedPlugins != null) {
      providers =
          dedupeByProviderClass(
              installedPlugins.loadInstalledServices(
                  NewsPublisherProfileProvider.class, BUILT_IN_NEWS_PROFILE_PROVIDERS));
    }
    return NewsPreviewUtil.publisherProfilesFromProviders(providers);
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
