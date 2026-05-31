package cafe.woden.ircclient.config;

import static cafe.woden.ircclient.config.RuntimeConfigYamlSupport.getOrCreateMapPath;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns tray settings under {@code ircafe.ui.tray}. */
class RuntimeConfigTrayStore {

  private static final Logger log = LoggerFactory.getLogger(RuntimeConfigTrayStore.class);

  private final Path file;
  private final RuntimeConfigDocumentStore documentStore;

  RuntimeConfigTrayStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.file = file;
    this.documentStore = documentStore;
  }

  synchronized Optional<Boolean> readCloseToTrayIfPresent() {
    try {
      if (file.toString().isBlank()) return Optional.empty();

      Map<String, Object> doc = documentStore.loadOrEmpty();
      Optional<Object> value =
          RuntimeConfigDocumentPathReader.readValue(doc, "ircafe", "ui", "tray", "closeToTray");
      if (value.isEmpty()) return Optional.empty();

      Object v = value.get();
      if (v instanceof Boolean b) return Optional.of(b);
      if (v instanceof String s) {
        String t = s.trim();
        if (t.equalsIgnoreCase("true")) return Optional.of(Boolean.TRUE);
        if (t.equalsIgnoreCase("false")) return Optional.of(Boolean.FALSE);
      }

      return Optional.empty();
    } catch (Exception e) {
      log.warn("[ircafe] Could not read tray.closeToTray from '{}'", file, e);
      return Optional.empty();
    }
  }

  synchronized boolean readCloseToTrayHintShown(boolean defaultValue) {
    try {
      if (file.toString().isBlank()) return defaultValue;

      Map<String, Object> doc = documentStore.loadOrEmpty();
      return RuntimeConfigDocumentPathReader.readValue(
              doc, "ircafe", "ui", "tray", "closeToTrayHintShown")
          .flatMap(RuntimeConfigYamlSupport::asBoolean)
          .orElse(defaultValue);
    } catch (Exception e) {
      log.warn("[ircafe] Could not read tray.closeToTrayHintShown from '{}'", file, e);
      return defaultValue;
    }
  }

  synchronized void rememberEnabled(boolean enabled) {
    rememberScalarSetting("enabled", enabled, "tray.enabled");
  }

  synchronized void rememberCloseToTray(boolean enabled) {
    rememberScalarSetting("closeToTray", enabled, "tray.closeToTray");
  }

  synchronized void rememberCloseToTrayHintShown(boolean shown) {
    rememberScalarSetting("closeToTrayHintShown", shown, "tray.closeToTrayHintShown");
  }

  synchronized void rememberMinimizeToTray(boolean enabled) {
    rememberScalarSetting("minimizeToTray", enabled, "tray.minimizeToTray");
  }

  synchronized void rememberStartMinimized(boolean enabled) {
    rememberScalarSetting("startMinimized", enabled, "tray.startMinimized");
  }

  synchronized void rememberNotifyHighlights(boolean enabled) {
    rememberScalarSetting("notifyHighlights", enabled, "tray.notifyHighlights");
  }

  synchronized void rememberNotifyPrivateMessages(boolean enabled) {
    rememberScalarSetting("notifyPrivateMessages", enabled, "tray.notifyPrivateMessages");
  }

  synchronized void rememberNotifyConnectionState(boolean enabled) {
    rememberScalarSetting("notifyConnectionState", enabled, "tray.notifyConnectionState");
  }

  synchronized void rememberNotifyOnlyWhenUnfocused(boolean enabled) {
    rememberScalarSetting("notifyOnlyWhenUnfocused", enabled, "tray.notifyOnlyWhenUnfocused");
  }

  synchronized void rememberNotifyOnlyWhenMinimizedOrHidden(boolean enabled) {
    rememberScalarSetting(
        "notifyOnlyWhenMinimizedOrHidden", enabled, "tray.notifyOnlyWhenMinimizedOrHidden");
  }

  synchronized void rememberNotifySuppressWhenTargetActive(boolean enabled) {
    rememberScalarSetting(
        "notifySuppressWhenTargetActive", enabled, "tray.notifySuppressWhenTargetActive");
  }

  synchronized void rememberLinuxDbusActionsEnabled(boolean enabled) {
    rememberScalarSetting("linuxDbusActionsEnabled", enabled, "tray.linuxDbusActionsEnabled");
  }

  synchronized void rememberNotificationBackend(String backendToken) {
    String v = Objects.toString(backendToken, "").trim().toLowerCase(Locale.ROOT);
    if (v.isEmpty()) v = "auto";
    rememberScalarSetting("notificationBackend", v, "tray.notificationBackend");
  }

  synchronized void rememberNotificationSoundsEnabled(boolean enabled) {
    rememberScalarSetting("notificationSoundsEnabled", enabled, "tray.notificationSoundsEnabled");
  }

  synchronized void rememberNotificationSound(String soundId) {
    String v = Objects.toString(soundId, "").trim();
    if (v.isEmpty()) v = "NOTIF_1";
    rememberScalarSetting("notificationSound", v, "tray.notificationSound");
  }

  synchronized void rememberNotificationSoundUseCustom(boolean useCustom) {
    rememberScalarSetting(
        "notificationSoundUseCustom", useCustom, "tray.notificationSoundUseCustom");
  }

  synchronized void rememberNotificationSoundCustomPath(String relativePath) {
    String v = Objects.toString(relativePath, "").trim();
    try {
      if (file.toString().isBlank()) return;

      Map<String, Object> doc = documentStore.loadOrEmpty();
      Map<String, Object> tray = getOrCreateMapPath(doc, "ircafe", "ui", "tray");
      if (v.isEmpty()) {
        tray.remove("notificationSoundCustomPath");
      } else {
        tray.put("notificationSoundCustomPath", v);
      }

      documentStore.write(doc);
    } catch (Exception e) {
      log.warn(
          "[ircafe] Could not persist tray.notificationSoundCustomPath setting to '{}'", file, e);
    }
  }

  private void rememberScalarSetting(String key, Object value, String description) {
    try {
      if (file.toString().isBlank()) return;

      Map<String, Object> doc = documentStore.loadOrEmpty();
      Map<String, Object> tray = getOrCreateMapPath(doc, "ircafe", "ui", "tray");

      tray.put(key, value);

      documentStore.write(doc);
    } catch (Exception e) {
      log.warn("[ircafe] Could not persist {} setting to '{}'", description, file, e);
    }
  }

}
