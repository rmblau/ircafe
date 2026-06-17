package cafe.woden.ircclient.ui.settings.notifications;

import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.notify.api.CustomSoundFileImportSupport;
import cafe.woden.ircclient.notify.api.CustomSoundPluginProviders;
import cafe.woden.ircclient.notify.spi.CustomSoundFileExtensionProvider;
import cafe.woden.ircclient.ui.localization.UiMessages;
import java.io.File;
import java.nio.file.Path;
import java.util.List;

public final class NotificationSoundFileImportSupport {
  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();

  private NotificationSoundFileImportSupport() {}

  public static String importToRuntimeDir(Path runtimeConfigPath, File source) throws Exception {
    return importToRuntimeDir(runtimeConfigPath, source, (InstalledPluginsPort) null);
  }

  public static String importToRuntimeDir(
      Path runtimeConfigPath, File source, InstalledPluginsPort installedPlugins) throws Exception {
    return importToRuntimeDir(
        runtimeConfigPath, source, CustomSoundPluginProviders.extensionProviders(installedPlugins));
  }

  static String importToRuntimeDir(
      Path runtimeConfigPath,
      File source,
      List<? extends CustomSoundFileExtensionProvider> extensionProviders)
      throws Exception {
    return CustomSoundFileImportSupport.importToRuntimeDir(
        runtimeConfigPath,
        source,
        extensionProviders,
        "notification",
        message("preferences.notifications.sound.import.invalidFileName"),
        message("preferences.notifications.sound.import.unsupportedType"),
        message("preferences.notifications.sound.import.unsupportedType"));
  }

  private static String message(String key, Object... args) {
    return MESSAGES.text(key, args);
  }
}
