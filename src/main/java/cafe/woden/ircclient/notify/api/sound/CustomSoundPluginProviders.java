package cafe.woden.ircclient.notify.api.sound;

import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.notify.spi.CustomSoundFileExtensionProvider;
import cafe.woden.ircclient.notify.spi.CustomSoundPlaybackProvider;
import cafe.woden.ircclient.util.PluginServiceLoaderSupport;
import java.util.List;

/** Centralizes ServiceLoader-backed custom sound plugin provider handling. */
public final class CustomSoundPluginProviders {
  private static final List<CustomSoundFileExtensionProvider> BUILT_IN_EXTENSION_PROVIDERS =
      CustomSoundProviderCatalog.extensionProviders(
          PluginServiceLoaderSupport.loadApplicationServices(
              CustomSoundFileExtensionProvider.class, CustomSoundPluginProviders.class),
          List.of());

  private CustomSoundPluginProviders() {}

  public static List<CustomSoundFileExtensionProvider> extensionProviders(
      InstalledPluginsPort installedPlugins) {
    if (installedPlugins == null) {
      return BUILT_IN_EXTENSION_PROVIDERS;
    }
    return CustomSoundProviderCatalog.extensionProviders(
        BUILT_IN_EXTENSION_PROVIDERS,
        installedPlugins.loadInstalledServices(
            CustomSoundFileExtensionProvider.class, BUILT_IN_EXTENSION_PROVIDERS));
  }

  public static List<CustomSoundPlaybackProvider> playbackProviders(
      InstalledPluginsPort installedPlugins) {
    if (installedPlugins == null) {
      return List.of();
    }
    return CustomSoundProviderCatalog.playbackProviders(
        installedPlugins.loadInstalledServices(CustomSoundPlaybackProvider.class, List.of()));
  }

  static List<CustomSoundFileExtensionProvider> builtInExtensionProviders() {
    return BUILT_IN_EXTENSION_PROVIDERS;
  }
}
