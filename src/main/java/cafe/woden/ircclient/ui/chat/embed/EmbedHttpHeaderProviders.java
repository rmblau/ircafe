package cafe.woden.ircclient.ui.chat.embed;

import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.ui.chat.embed.spi.EmbedHttpHeaderProvider;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.jmolecules.architecture.layered.InterfaceLayer;
import org.slf4j.Logger;

/** Shared helpers for ServiceLoader-backed embed HTTP header providers. */
@InterfaceLayer
final class EmbedHttpHeaderProviders {

  private static final LinkPreviewHttpHeaderCatalog CATALOG = new LinkPreviewHttpHeaderCatalog();

  private EmbedHttpHeaderProviders() {}

  static List<EmbedHttpHeaderProvider> loadInstalledProviders(
      InstalledPluginsPort installedPlugins) {
    if (installedPlugins == null) return List.of();
    List<EmbedHttpHeaderProvider> installed =
        installedPlugins.loadInstalledServices(EmbedHttpHeaderProvider.class, List.of());
    return CATALOG.headerProviders(List.of(), installed);
  }

  static void applyProviderHeaders(
      Map<String, String> headers,
      URI uri,
      List<? extends EmbedHttpHeaderProvider> providers,
      Logger log,
      String providerDescription) {
    LinkPreviewHttpHeaderResult result = CATALOG.applyProviderHeaders(headers, uri, providers);
    headers.clear();
    headers.putAll(result.headers());
    for (LinkPreviewHttpHeaderProviderFailure failure : result.failures()) {
      EmbedHttpHeaderProvider provider = failure.provider();
      log.warn(
          "{} failed: {}", providerDescription, provider.getClass().getName(), failure.error());
    }
  }
}
