package cafe.woden.ircclient.config;

import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSection;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns tray settings under {@code ircafe.ui.tray}. */
class RuntimeConfigTrayStore {

  private static final Logger log = LoggerFactory.getLogger(RuntimeConfigTrayStore.class);

  private final RuntimeConfigYamlSection traySection;

  RuntimeConfigTrayStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.traySection =
        RuntimeConfigYamlSection.ircafeUi(file, documentStore, log, "tray");
  }

  synchronized Optional<Boolean> readCloseToTrayIfPresent() {
    return traySection.readValue("tray.closeToTray", "closeToTray")
        .flatMap(RuntimeConfigYamlSupport::asBoolean);
  }

  synchronized boolean readCloseToTrayHintShown(boolean defaultValue) {
    return traySection.readValue("tray.closeToTrayHintShown", "closeToTrayHintShown")
        .flatMap(RuntimeConfigYamlSupport::asBoolean)
        .orElse(defaultValue);
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
    if (v.isEmpty()) {
      traySection.removeExistingValueAndPruneEmptyParents(
          "tray.notificationSoundCustomPath", "notificationSoundCustomPath");
      return;
    }

    rememberScalarSetting("notificationSoundCustomPath", v, "tray.notificationSoundCustomPath");
  }

  private void rememberScalarSetting(String key, Object value, String description) {
    traySection.putValue(description, value, key);
  }

}
