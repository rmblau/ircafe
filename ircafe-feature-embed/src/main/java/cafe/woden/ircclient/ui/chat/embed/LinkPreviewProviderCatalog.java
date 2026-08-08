package cafe.woden.ircclient.ui.chat.embed;

import cafe.woden.ircclient.ui.chat.embed.spi.BuiltInLinkPreviewResolverOrders;
import cafe.woden.ircclient.ui.chat.embed.spi.ImageUrlExtensionProvider;
import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreviewResolver;
import cafe.woden.ircclient.ui.chat.embed.spi.NewsPublisherProfile;
import cafe.woden.ircclient.ui.chat.embed.spi.NewsPublisherProfileProvider;
import cafe.woden.ircclient.ui.chat.embed.spi.OEmbedLinkPreviewProvider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import org.jmolecules.architecture.layered.InterfaceLayer;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/** Pure catalog rules for embed/link-preview provider chains. */
@Component
@InterfaceLayer
@Lazy
public class LinkPreviewProviderCatalog {

  public List<LinkPreviewResolver> linkPreviewResolvers(
      Collection<? extends LinkPreviewResolver> builtInResolvers,
      Collection<? extends LinkPreviewResolver> installedResolvers) {
    return dedupeByProviderClass(builtInResolvers, installedResolvers).stream()
        .sorted(Comparator.comparingInt(LinkPreviewProviderCatalog::resolverSortOrder))
        .toList();
  }

  public List<OEmbedLinkPreviewProvider> oEmbedProviders(
      Collection<? extends OEmbedLinkPreviewProvider> builtInProviders,
      Collection<? extends OEmbedLinkPreviewProvider> installedProviders) {
    return dedupeByProviderClass(builtInProviders, installedProviders);
  }

  public List<NewsPublisherProfileProvider> newsPublisherProfileProviders(
      Collection<? extends NewsPublisherProfileProvider> builtInProviders,
      Collection<? extends NewsPublisherProfileProvider> installedProviders) {
    return dedupeByProviderClass(builtInProviders, installedProviders);
  }

  public List<NewsPublisherProfile> newsPublisherProfiles(
      Collection<? extends NewsPublisherProfileProvider> profileProviders) {
    ArrayList<NewsPublisherProfile> profiles = new ArrayList<>();
    for (NewsPublisherProfileProvider provider : safeList(profileProviders)) {
      if (provider == null) continue;
      try {
        List<NewsPublisherProfile> contributed = provider.publisherProfiles();
        if (contributed == null || contributed.isEmpty()) continue;
        for (NewsPublisherProfile profile : contributed) {
          if (profile != null) profiles.add(profile);
        }
      } catch (RuntimeException ignored) {
        // Provider errors are intentionally isolated from catalog construction.
      }
    }
    return List.copyOf(profiles);
  }

  public List<ImageUrlExtensionProvider> imageUrlExtensionProviders(
      Collection<? extends ImageUrlExtensionProvider> builtInProviders,
      Collection<? extends ImageUrlExtensionProvider> installedProviders) {
    return dedupeByProviderClass(builtInProviders, installedProviders);
  }

  public Set<String> imageExtensions(
      Collection<? extends ImageUrlExtensionProvider> extensionProviders) {
    LinkedHashSet<String> extensions = new LinkedHashSet<>();
    for (ImageUrlExtensionProvider provider : safeList(extensionProviders)) {
      if (provider == null) continue;
      try {
        List<String> contributed = provider.imageFileExtensions();
        if (contributed == null || contributed.isEmpty()) continue;
        for (String extension : contributed) {
          String normalized = normalizeImageExtension(extension);
          if (normalized != null) extensions.add(normalized);
        }
      } catch (RuntimeException ignored) {
        // Provider errors are intentionally isolated from catalog construction.
      }
    }
    return Collections.unmodifiableSet(extensions);
  }

  private static int resolverSortOrder(LinkPreviewResolver resolver) {
    try {
      return resolver == null
          ? BuiltInLinkPreviewResolverOrders.PLUGIN_DEFAULT
          : resolver.sortOrder();
    } catch (RuntimeException ex) {
      return BuiltInLinkPreviewResolverOrders.PLUGIN_DEFAULT;
    }
  }

  @SafeVarargs
  private static <T> List<T> dedupeByProviderClass(Collection<? extends T>... providerLists) {
    LinkedHashMap<Class<?>, T> deduped = new LinkedHashMap<>();
    for (Collection<? extends T> providers : providerLists) {
      for (T provider : safeList(providers)) {
        if (provider == null) continue;
        deduped.putIfAbsent(provider.getClass(), provider);
      }
    }
    return List.copyOf(deduped.values());
  }

  private static <T> Collection<? extends T> safeList(Collection<? extends T> providers) {
    return providers == null ? List.of() : providers;
  }

  private static String normalizeImageExtension(String value) {
    String normalized = Objects.toString(value, "").trim().toLowerCase(Locale.ROOT);
    if (normalized.isEmpty()) return null;
    if (!normalized.startsWith(".")) normalized = "." + normalized;
    if (normalized.indexOf('/', 1) >= 0 || normalized.indexOf('\\', 1) >= 0) return null;
    return normalized;
  }
}
