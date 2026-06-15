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

  static <T extends EmbedHttpHeaderProvider> List<EmbedHttpHeaderProvider> loadInstalledProviders(
      InstalledPluginsPort installedPlugins, Class<T> specificProviderType) {
    if (installedPlugins == null) return List.of();
    List<EmbedHttpHeaderProvider> sharedProviders =
        installedPlugins.loadInstalledServices(EmbedHttpHeaderProvider.class, List.of());
    List<T> specificProviders =
        installedPlugins.loadInstalledServices(specificProviderType, List.of());
    return merge(sharedProviders, specificProviders);
  }

  static void applyProviderHeaders(
      Map<String, String> headers,
      URI uri,
      List<? extends EmbedHttpHeaderProvider> providers,
      Logger log,
      String providerDescription) {
    List<? extends EmbedHttpHeaderProvider> safeProviders =
        providers == null ? List.of() : providers;
    for (EmbedHttpHeaderProvider provider : safeProviders) {
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

  private static List<EmbedHttpHeaderProvider> merge(
      List<? extends EmbedHttpHeaderProvider> sharedProviders,
      List<? extends EmbedHttpHeaderProvider> specificProviders) {
    List<EmbedHttpHeaderProvider> merged = new ArrayList<>();
    Set<String> seenProviderTypes = new LinkedHashSet<>();
    addHeaderProviders(merged, seenProviderTypes, sharedProviders);
    addHeaderProviders(merged, seenProviderTypes, specificProviders);
    return List.copyOf(merged);
  }

  private static void addHeaderProviders(
      List<EmbedHttpHeaderProvider> merged,
      Set<String> seenProviderTypes,
      List<? extends EmbedHttpHeaderProvider> providers) {
    List<? extends EmbedHttpHeaderProvider> safeProviders =
        providers == null ? List.of() : providers;
    for (EmbedHttpHeaderProvider provider : safeProviders) {
      if (provider == null) continue;
      String providerType = provider.getClass().getName();
      if (seenProviderTypes.add(providerType)) {
        merged.add(provider);
      }
    }
  }
}
