package cafe.woden.ircclient.ui.chat.embed;

import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.ui.chat.embed.spi.ImageUrlExtensionProvider;
import cafe.woden.ircclient.util.PluginServiceLoaderSupport;
import java.util.List;
import java.util.Set;
import org.jmolecules.architecture.layered.InterfaceLayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Root bridge for ServiceLoader-backed image URL extension providers. */
@InterfaceLayer
final class ImageUrlExtensionProviders {

  private static final Logger log = LoggerFactory.getLogger(ImageUrlExtensionProviders.class);
  private static final LinkPreviewProviderCatalog CATALOG = new LinkPreviewProviderCatalog();

  private ImageUrlExtensionProviders() {}

  static List<ImageUrlExtensionProvider> loadInstalledProviders(
      InstalledPluginsPort installedPlugins) {
    List<ImageUrlExtensionProvider> builtInProviders = builtInExtensionProviders();
    if (installedPlugins == null) return builtInProviders;
    return CATALOG.imageUrlExtensionProviders(
        builtInProviders,
        installedPlugins.loadInstalledServices(ImageUrlExtensionProvider.class, builtInProviders));
  }

  static Set<String> imageExtensions(List<? extends ImageUrlExtensionProvider> extensionProviders) {
    return CATALOG.imageExtensions(extensionProviderChain(extensionProviders));
  }

  private static List<ImageUrlExtensionProvider> extensionProviderChain(
      List<? extends ImageUrlExtensionProvider> extensionProviders) {
    return CATALOG.imageUrlExtensionProviders(builtInExtensionProviders(), extensionProviders);
  }

  private static List<ImageUrlExtensionProvider> builtInExtensionProviders() {
    try {
      return CATALOG.imageUrlExtensionProviders(
          PluginServiceLoaderSupport.loadApplicationServices(
              ImageUrlExtensionProvider.class, ImageUrlExtensionProviders.class),
          List.of());
    } catch (RuntimeException ex) {
      log.warn("[ircafe] failed to load built-in image URL extension providers", ex);
      return List.of();
    }
  }
}
