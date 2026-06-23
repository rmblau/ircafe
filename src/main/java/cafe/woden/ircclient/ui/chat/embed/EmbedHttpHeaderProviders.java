package cafe.woden.ircclient.ui.chat.embed;

import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.jmolecules.architecture.layered.InterfaceLayer;
import org.slf4j.Logger;

/** Shared helpers for ServiceLoader-backed embed HTTP header providers. */
@InterfaceLayer
final class EmbedHttpHeaderProviders {

  private EmbedHttpHeaderProviders() {}

  static List<cafe.woden.ircclient.ui.chat.embed.spi.EmbedHttpHeaderProvider>
      loadInstalledProviders(InstalledPluginsPort installedPlugins) {
    if (installedPlugins == null) return List.of();
    return installedPlugins.loadInstalledServices(
        cafe.woden.ircclient.ui.chat.embed.spi.EmbedHttpHeaderProvider.class, List.of());
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
}
