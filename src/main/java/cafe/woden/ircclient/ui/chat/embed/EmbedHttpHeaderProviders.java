package cafe.woden.ircclient.ui.chat.embed;

import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.jmolecules.architecture.layered.InterfaceLayer;
import org.slf4j.Logger;

/** Shared helpers for ServiceLoader-backed embed HTTP header providers. */
@InterfaceLayer
final class EmbedHttpHeaderProviders {

  private EmbedHttpHeaderProviders() {}

  static <T extends cafe.woden.ircclient.ui.chat.embed.spi.EmbedHttpHeaderProvider>
      List<cafe.woden.ircclient.ui.chat.embed.spi.EmbedHttpHeaderProvider> loadInstalledProviders(
          InstalledPluginsPort installedPlugins, Class<T> specificProviderType) {
    if (installedPlugins == null) return List.of();
    List<cafe.woden.ircclient.ui.chat.embed.spi.EmbedHttpHeaderProvider> spiProviders =
        installedPlugins.loadInstalledServices(
            cafe.woden.ircclient.ui.chat.embed.spi.EmbedHttpHeaderProvider.class, List.of());
    List<EmbedHttpHeaderProvider> legacySharedProviders =
        installedPlugins.loadInstalledServices(EmbedHttpHeaderProvider.class, List.of());
    List<T> legacySpecificProviders =
        specificProviderType == null
            ? List.of()
            : installedPlugins.loadInstalledServices(specificProviderType, List.of());
    return merge(spiProviders, legacySharedProviders, legacySpecificProviders);
  }

  static void applyProviderHeaders(
      Map<String, String> headers,
      URI uri,
      List<? extends cafe.woden.ircclient.ui.chat.embed.spi.EmbedHttpHeaderProvider> providers,
      Logger log,
      String providerDescription) {
    List<? extends cafe.woden.ircclient.ui.chat.embed.spi.EmbedHttpHeaderProvider> safeProviders =
        providers == null ? List.of() : providers;
    for (cafe.woden.ircclient.ui.chat.embed.spi.EmbedHttpHeaderProvider provider : safeProviders) {
      if (provider == null) continue;
      try {
        Map<String, String> provided = provider.embedHttpHeaders(uri);
        if (provided == null || provided.isEmpty()) continue;
        for (Map.Entry<String, String> entry : provided.entrySet()) {
          String name = Objects.toString(entry.getKey(), "").trim();
          String value = Objects.toString(entry.getValue(), "").trim();
          if (!name.isEmpty() && !value.isEmpty()) headers.put(name, value);
        }
      } catch (RuntimeException ex) {
        log.warn("{} failed: {}", providerDescription, provider.getClass().getName(), ex);
      }
    }
  }

  @SafeVarargs
  private static List<cafe.woden.ircclient.ui.chat.embed.spi.EmbedHttpHeaderProvider> merge(
      List<? extends cafe.woden.ircclient.ui.chat.embed.spi.EmbedHttpHeaderProvider>...
          providerGroups) {
    List<cafe.woden.ircclient.ui.chat.embed.spi.EmbedHttpHeaderProvider> merged = new ArrayList<>();
    Set<String> seenProviderTypes = new LinkedHashSet<>();
    for (List<? extends cafe.woden.ircclient.ui.chat.embed.spi.EmbedHttpHeaderProvider> providers :
        providerGroups) {
      addHeaderProviders(merged, seenProviderTypes, providers);
    }
    return List.copyOf(merged);
  }

  private static void addHeaderProviders(
      List<cafe.woden.ircclient.ui.chat.embed.spi.EmbedHttpHeaderProvider> merged,
      Set<String> seenProviderTypes,
      List<? extends cafe.woden.ircclient.ui.chat.embed.spi.EmbedHttpHeaderProvider> providers) {
    List<? extends cafe.woden.ircclient.ui.chat.embed.spi.EmbedHttpHeaderProvider> safeProviders =
        providers == null ? List.of() : providers;
    for (cafe.woden.ircclient.ui.chat.embed.spi.EmbedHttpHeaderProvider provider : safeProviders) {
      if (provider == null) continue;
      String providerType = provider.getClass().getName();
      if (seenProviderTypes.add(providerType)) {
        merged.add(provider);
      }
    }
  }
}
