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

  static List<ImageUrlExtensionProvider> loadInstalledProviders(
      InstalledPluginsPort installedPlugins) {
    if (installedPlugins == null) return List.of();
    return List.copyOf(
        Objects.requireNonNullElse(
            installedPlugins.loadInstalledServices(ImageUrlExtensionProvider.class, List.of()),
            List.<ImageUrlExtensionProvider>of()));
  }

  static Set<String> imageExtensions(List<ImageUrlExtensionProvider> extensionProviders) {
    LinkedHashSet<String> extensions = new LinkedHashSet<>(DEFAULT_IMAGE_EXTENSIONS);
    for (ImageUrlExtensionProvider provider :
        Objects.requireNonNullElse(
            extensionProviders, Collections.<ImageUrlExtensionProvider>emptyList())) {
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

  private static String normalizeImageExtension(String value) {
    String normalized = Objects.toString(value, "").trim().toLowerCase(Locale.ROOT);
    if (normalized.isEmpty()) return null;
    if (!normalized.startsWith(".")) normalized = "." + normalized;
    if (normalized.indexOf('/', 1) >= 0 || normalized.indexOf('\\', 1) >= 0) return null;
    return normalized;
  }
}
