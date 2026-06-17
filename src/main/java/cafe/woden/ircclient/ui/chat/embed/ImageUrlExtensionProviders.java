package cafe.woden.ircclient.ui.chat.embed;

import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import org.jmolecules.architecture.layered.InterfaceLayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Shared helpers for ServiceLoader-backed image URL extension providers. */
@InterfaceLayer
final class ImageUrlExtensionProviders {

  private static final Logger log = LoggerFactory.getLogger(ImageUrlExtensionProviders.class);

  private static final List<String> DEFAULT_IMAGE_EXTENSIONS =
      List.of(".png", ".jpg", ".jpeg", ".gif", ".webp");

  private ImageUrlExtensionProviders() {}

  @SuppressWarnings("deprecation")
  static List<cafe.woden.ircclient.ui.chat.embed.spi.ImageUrlExtensionProvider>
      loadInstalledProviders(InstalledPluginsPort installedPlugins) {
    if (installedPlugins == null) return List.of();
    List<cafe.woden.ircclient.ui.chat.embed.spi.ImageUrlExtensionProvider> providers =
        new java.util.ArrayList<>();
    providers.addAll(
        Objects.requireNonNullElse(
            installedPlugins.loadInstalledServices(
                cafe.woden.ircclient.ui.chat.embed.spi.ImageUrlExtensionProvider.class, List.of()),
            List.<cafe.woden.ircclient.ui.chat.embed.spi.ImageUrlExtensionProvider>of()));
    providers.addAll(
        Objects.requireNonNullElse(
            installedPlugins.loadInstalledServices(ImageUrlExtensionProvider.class, List.of()),
            List.<ImageUrlExtensionProvider>of()));
    return dedupeByProviderClass(providers);
  }

  static Set<String> imageExtensions(
      List<? extends cafe.woden.ircclient.ui.chat.embed.spi.ImageUrlExtensionProvider>
          extensionProviders) {
    LinkedHashSet<String> extensions = new LinkedHashSet<>(DEFAULT_IMAGE_EXTENSIONS);
    for (cafe.woden.ircclient.ui.chat.embed.spi.ImageUrlExtensionProvider provider :
        Objects.requireNonNullElse(
            extensionProviders,
            Collections
                .<cafe.woden.ircclient.ui.chat.embed.spi.ImageUrlExtensionProvider>emptyList())) {
      if (provider == null) continue;
      try {
        List<String> contributed = provider.imageFileExtensions();
        if (contributed == null || contributed.isEmpty()) continue;
        for (String extension : contributed) {
          String normalized = normalizeImageExtension(extension);
          if (normalized != null) extensions.add(normalized);
        }
      } catch (RuntimeException ex) {
        log.warn(
            "[ircafe] failed to load image URL extensions from {}",
            provider.getClass().getName(),
            ex);
      }
    }
    return Collections.unmodifiableSet(extensions);
  }

  private static List<cafe.woden.ircclient.ui.chat.embed.spi.ImageUrlExtensionProvider>
      dedupeByProviderClass(
          List<? extends cafe.woden.ircclient.ui.chat.embed.spi.ImageUrlExtensionProvider>
              providers) {
    java.util.LinkedHashSet<String> providerClassNames = new java.util.LinkedHashSet<>();
    java.util.ArrayList<cafe.woden.ircclient.ui.chat.embed.spi.ImageUrlExtensionProvider> deduped =
        new java.util.ArrayList<>();
    for (cafe.woden.ircclient.ui.chat.embed.spi.ImageUrlExtensionProvider provider :
        Objects.requireNonNullElse(
            providers,
            List.<cafe.woden.ircclient.ui.chat.embed.spi.ImageUrlExtensionProvider>of())) {
      if (provider == null || !providerClassNames.add(provider.getClass().getName())) {
        continue;
      }
      deduped.add(provider);
    }
    return List.copyOf(deduped);
  }

  private static String normalizeImageExtension(String value) {
    String normalized = Objects.toString(value, "").trim().toLowerCase(Locale.ROOT);
    if (normalized.isEmpty()) return null;
    if (!normalized.startsWith(".")) normalized = "." + normalized;
    if (normalized.indexOf('/', 1) >= 0 || normalized.indexOf('\\', 1) >= 0) return null;
    return normalized;
  }
}
