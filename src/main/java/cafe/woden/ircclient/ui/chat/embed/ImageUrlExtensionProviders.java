package cafe.woden.ircclient.ui.chat.embed;

import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.ui.chat.embed.builtins.BuiltInImageUrlExtensionProvider;
import cafe.woden.ircclient.ui.chat.embed.spi.ImageUrlExtensionProvider;
import java.util.ArrayList;
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

  private static final List<ImageUrlExtensionProvider> BUILT_IN_EXTENSION_PROVIDERS =
      List.of(new BuiltInImageUrlExtensionProvider());

  private ImageUrlExtensionProviders() {}

  static List<ImageUrlExtensionProvider> loadInstalledProviders(
      InstalledPluginsPort installedPlugins) {
    if (installedPlugins == null) return BUILT_IN_EXTENSION_PROVIDERS;
    return dedupeByProviderClass(
        installedPlugins.loadInstalledServices(
            ImageUrlExtensionProvider.class, BUILT_IN_EXTENSION_PROVIDERS));
  }

  static Set<String> imageExtensions(List<? extends ImageUrlExtensionProvider> extensionProviders) {
    LinkedHashSet<String> extensions = new LinkedHashSet<>();
    for (ImageUrlExtensionProvider provider : extensionProviderChain(extensionProviders)) {
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

  private static List<ImageUrlExtensionProvider> extensionProviderChain(
      List<? extends ImageUrlExtensionProvider> extensionProviders) {
    LinkedHashSet<String> providerClassNames = new LinkedHashSet<>();
    ArrayList<ImageUrlExtensionProvider> providers = new ArrayList<>();
    addProviders(providers, providerClassNames, BUILT_IN_EXTENSION_PROVIDERS);
    addProviders(providers, providerClassNames, extensionProviders);
    return List.copyOf(providers);
  }

  private static void addProviders(
      ArrayList<ImageUrlExtensionProvider> providers,
      LinkedHashSet<String> providerClassNames,
      List<? extends ImageUrlExtensionProvider> candidates) {
    for (ImageUrlExtensionProvider provider :
        Objects.requireNonNullElse(candidates, List.<ImageUrlExtensionProvider>of())) {
      if (provider == null || !providerClassNames.add(provider.getClass().getName())) {
        continue;
      }
      providers.add(provider);
    }
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

  private static String normalizeImageExtension(String value) {
    String normalized = Objects.toString(value, "").trim().toLowerCase(Locale.ROOT);
    if (normalized.isEmpty()) return null;
    if (!normalized.startsWith(".")) normalized = "." + normalized;
    if (normalized.indexOf('/', 1) >= 0 || normalized.indexOf('\\', 1) >= 0) return null;
    return normalized;
  }
}
