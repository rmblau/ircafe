package cafe.woden.ircclient.notify.api;

import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.notify.builtins.BuiltInCustomSoundFileExtensionProvider;
import cafe.woden.ircclient.notify.spi.CustomSoundFileExtensionProvider;
import cafe.woden.ircclient.notify.spi.CustomSoundPlaybackProvider;
import java.util.List;

/** Centralizes ServiceLoader-backed custom sound plugin provider handling. */
public final class CustomSoundPluginProviders {
  private static final List<CustomSoundFileExtensionProvider> BUILT_IN_EXTENSION_PROVIDERS =
      List.of(new BuiltInCustomSoundFileExtensionProvider());

  private CustomSoundPluginProviders() {}

  public static List<CustomSoundFileExtensionProvider> extensionProviders(
      InstalledPluginsPort installedPlugins) {
    if (installedPlugins == null) {
      return BUILT_IN_EXTENSION_PROVIDERS;
    }
    return dedupeByProviderClass(
        installedPlugins.loadInstalledServices(
            CustomSoundFileExtensionProvider.class, BUILT_IN_EXTENSION_PROVIDERS));
  }

  public static List<CustomSoundPlaybackProvider> playbackProviders(
      InstalledPluginsPort installedPlugins) {
    if (installedPlugins == null) {
      return List.of();
    }
    return dedupeByProviderClass(
        installedPlugins.loadInstalledServices(CustomSoundPlaybackProvider.class, List.of()));
  }

  static List<CustomSoundFileExtensionProvider> builtInExtensionProviders() {
    return BUILT_IN_EXTENSION_PROVIDERS;
  }

  private static <T> List<T> nonNullServices(List<? extends T> services) {
    if (services == null || services.isEmpty()) {
      return List.of();
    }
    java.util.ArrayList<T> nonNull = new java.util.ArrayList<>();
    for (T service : services) {
      if (service != null) {
        nonNull.add(service);
      }
    }
    return List.copyOf(nonNull);
  }

  private static <T> List<T> dedupeByProviderClass(List<? extends T> services) {
    java.util.LinkedHashSet<String> providerClassNames = new java.util.LinkedHashSet<>();
    java.util.ArrayList<T> deduped = new java.util.ArrayList<>();
    for (T service : nonNullServices(services)) {
      if (!providerClassNames.add(service.getClass().getName())) {
        continue;
      }
      deduped.add(service);
    }
    return List.copyOf(deduped);
  }
}
