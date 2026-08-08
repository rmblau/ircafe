package cafe.woden.ircclient.config.runtime.ui;

import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSection;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport;
import java.nio.file.Path;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns tray settings under {@code ircafe.ui.tray}. */
public class RuntimeConfigTrayStore {

  private static final Logger log = LoggerFactory.getLogger(RuntimeConfigTrayStore.class);

  private final RuntimeConfigYamlSection traySection;

  public RuntimeConfigTrayStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.traySection = RuntimeConfigYamlSection.ircafeUi(file, documentStore, log, "tray");
  }

  public synchronized Optional<Boolean> readCloseToTrayIfPresent() {
    return traySection
        .readValue("tray.closeToTray", "closeToTray")
        .flatMap(RuntimeConfigYamlSupport::asBoolean);
  }

  public synchronized boolean readCloseToTrayHintShown(boolean defaultValue) {
    return traySection
        .readValue("tray.closeToTrayHintShown", "closeToTrayHintShown")
        .flatMap(RuntimeConfigYamlSupport::asBoolean)
        .orElse(defaultValue);
  }

  public synchronized void rememberEnabled(boolean enabled) {
    rememberScalarSetting("enabled", enabled, "tray.enabled");
  }

  public synchronized void rememberCloseToTray(boolean enabled) {
    rememberScalarSetting("closeToTray", enabled, "tray.closeToTray");
  }

  public synchronized void rememberCloseToTrayHintShown(boolean shown) {
    rememberScalarSetting("closeToTrayHintShown", shown, "tray.closeToTrayHintShown");
  }

  public synchronized void rememberMinimizeToTray(boolean enabled) {
    rememberScalarSetting("minimizeToTray", enabled, "tray.minimizeToTray");
  }

  public synchronized void rememberStartMinimized(boolean enabled) {
    rememberScalarSetting("startMinimized", enabled, "tray.startMinimized");
  }

  public synchronized void rememberNotifyHighlights(boolean enabled) {
    rememberScalarSetting("notifyHighlights", enabled, "tray.notifyHighlights");
  }

  public synchronized void rememberNotifyPrivateMessages(boolean enabled) {
    rememberScalarSetting("notifyPrivateMessages", enabled, "tray.notifyPrivateMessages");
  }

  public synchronized void rememberNotifyConnectionState(boolean enabled) {
    rememberScalarSetting("notifyConnectionState", enabled, "tray.notifyConnectionState");
  }

  public synchronized void rememberNotifyOnlyWhenUnfocused(boolean enabled) {
    rememberScalarSetting("notifyOnlyWhenUnfocused", enabled, "tray.notifyOnlyWhenUnfocused");
  }

  public synchronized void rememberNotifyOnlyWhenMinimizedOrHidden(boolean enabled) {
    rememberScalarSetting(
        "notifyOnlyWhenMinimizedOrHidden", enabled, "tray.notifyOnlyWhenMinimizedOrHidden");
  }

  public synchronized void rememberNotifySuppressWhenTargetActive(boolean enabled) {
    rememberScalarSetting(
        "notifySuppressWhenTargetActive", enabled, "tray.notifySuppressWhenTargetActive");
  }

  public synchronized void rememberLinuxDbusActionsEnabled(boolean enabled) {
    rememberScalarSetting("linuxDbusActionsEnabled", enabled, "tray.linuxDbusActionsEnabled");
  }

  public synchronized void rememberNotificationBackend(String backendToken) {
    String v = RuntimeConfigTraySettingsCodec.normalizeNotificationBackend(backendToken);
    rememberScalarSetting("notificationBackend", v, "tray.notificationBackend");
  }

  public synchronized void rememberNotificationSoundsEnabled(boolean enabled) {
    rememberScalarSetting("notificationSoundsEnabled", enabled, "tray.notificationSoundsEnabled");
  }

  public synchronized void rememberNotificationSound(String soundId) {
    String v = RuntimeConfigTraySettingsCodec.normalizeNotificationSound(soundId);
    rememberScalarSetting("notificationSound", v, "tray.notificationSound");
  }

  public synchronized void rememberNotificationSoundUseCustom(boolean useCustom) {
    rememberScalarSetting(
        "notificationSoundUseCustom", useCustom, "tray.notificationSoundUseCustom");
  }

  public synchronized void rememberNotificationSoundCustomPath(String relativePath) {
    String v = RuntimeConfigTraySettingsCodec.normalizeNotificationSoundCustomPath(relativePath);
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
