package cafe.woden.ircclient.notify.api;

import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import java.util.ArrayList;
import java.util.List;

/** Centralizes ServiceLoader-backed custom sound plugin provider handling. */
public final class CustomSoundPluginProviders {
  private CustomSoundPluginProviders() {}

  public static List<cafe.woden.ircclient.notify.spi.CustomSoundFileExtensionProvider>
      extensionProviders(InstalledPluginsPort installedPlugins) {
    if (installedPlugins == null) {
      return List.of();
    }
    ArrayList<cafe.woden.ircclient.notify.spi.CustomSoundFileExtensionProvider> providers =
        new ArrayList<>();
    providers.addAll(
        installedPlugins.loadInstalledServices(
            cafe.woden.ircclient.notify.spi.CustomSoundFileExtensionProvider.class, List.of()));
    providers.addAll(
        installedPlugins.loadInstalledServices(CustomSoundFileExtensionProvider.class, List.of()));
    return dedupeByProviderClass(providers);
  }

  public static List<cafe.woden.ircclient.notify.spi.CustomSoundPlaybackProvider> playbackProviders(
      InstalledPluginsPort installedPlugins) {
    if (installedPlugins == null) {
      return List.of();
    }
    ArrayList<cafe.woden.ircclient.notify.spi.CustomSoundPlaybackProvider> providers =
        new ArrayList<>();
    providers.addAll(
        installedPlugins.loadInstalledServices(
            cafe.woden.ircclient.notify.spi.CustomSoundPlaybackProvider.class, List.of()));
    providers.addAll(
        installedPlugins.loadInstalledServices(CustomSoundPlaybackProvider.class, List.of()));
    return dedupeByProviderClass(providers);
  }

  private static <T> List<T> nonNullServices(List<? extends T> services) {
    if (services == null || services.isEmpty()) {
      return List.of();
    }
    ArrayList<T> nonNull = new ArrayList<>();
    for (T service : services) {
      if (service != null) {
        nonNull.add(service);
      }
    }
    return List.copyOf(nonNull);
  }

  private static <T> List<T> dedupeByProviderClass(List<? extends T> services) {
    java.util.LinkedHashSet<String> providerClassNames = new java.util.LinkedHashSet<>();
    ArrayList<T> deduped = new ArrayList<>();
    for (T service : nonNullServices(services)) {
      if (!providerClassNames.add(service.getClass().getName())) {
        continue;
      }
      deduped.add(service);
    }
    return List.copyOf(deduped);
  }
}
