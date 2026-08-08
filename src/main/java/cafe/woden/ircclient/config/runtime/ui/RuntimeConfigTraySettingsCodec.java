package cafe.woden.ircclient.config.runtime.ui;

import java.util.Locale;
import java.util.Objects;

/** Pure normalization helpers for persisted tray settings. */
final class RuntimeConfigTraySettingsCodec {

  private RuntimeConfigTraySettingsCodec() {}

  static String normalizeNotificationBackend(String backendToken) {
    String value = Objects.toString(backendToken, "").trim().toLowerCase(Locale.ROOT);
    return value.isEmpty() ? "auto" : value;
  }

  static String normalizeNotificationSound(String soundId) {
    String value = Objects.toString(soundId, "").trim();
    return value.isEmpty() ? "NOTIF_1" : value;
  }

  static String normalizeNotificationSoundCustomPath(String relativePath) {
    return Objects.toString(relativePath, "").trim();
  }
}
