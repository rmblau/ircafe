package cafe.woden.ircclient.notify.api;

import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import java.util.List;
import java.util.Objects;

/** Centralizes ServiceLoader-backed custom sound plugin provider handling. */
public final class CustomSoundPluginProviders {
  private CustomSoundPluginProviders() {}

  public static List<CustomSoundFileExtensionProvider> extensionProviders(
      InstalledPluginsPort installedPlugins) {
    if (installedPlugins == null) {
      return List.of();
    }
    return nonNullServices(
        installedPlugins.loadInstalledServices(CustomSoundFileExtensionProvider.class, List.of()));
  }

  public static List<CustomSoundPlaybackProvider> playbackProviders(
      InstalledPluginsPort installedPlugins) {
    if (installedPlugins == null) {
      return List.of();
    }
    return nonNullServices(
        installedPlugins.loadInstalledServices(CustomSoundPlaybackProvider.class, List.of()));
  }

  private static <T> List<T> nonNullServices(List<T> services) {
    if (services == null || services.isEmpty()) {
      return List.of();
    }
    return services.stream().filter(Objects::nonNull).toList();
  }
}
