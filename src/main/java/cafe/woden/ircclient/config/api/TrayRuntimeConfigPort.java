package cafe.woden.ircclient.config.api;

import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.architecture.layered.ApplicationLayer;

/** Runtime-config contract for persisted tray and notification-sound settings. */
@SecondaryPort
@ApplicationLayer
public interface TrayRuntimeConfigPort extends RuntimeConfigPathPort {

  void rememberTrayEnabled(boolean enabled);

  void rememberTrayCloseToTray(boolean enabled);

  void rememberTrayMinimizeToTray(boolean enabled);

  void rememberTrayStartMinimized(boolean enabled);

  void rememberTrayNotifyHighlights(boolean enabled);

  void rememberTrayNotifyPrivateMessages(boolean enabled);

  void rememberTrayNotifyConnectionState(boolean enabled);

  void rememberTrayNotifyOnlyWhenUnfocused(boolean enabled);

  void rememberTrayNotifyOnlyWhenMinimizedOrHidden(boolean enabled);

  void rememberTrayNotifySuppressWhenTargetActive(boolean enabled);

  void rememberTrayLinuxDbusActionsEnabled(boolean enabled);

  void rememberTrayNotificationBackend(String backendToken);

  void rememberTrayNotificationSoundsEnabled(boolean enabled);

  void rememberTrayNotificationSound(String soundId);

  void rememberTrayNotificationSoundUseCustom(boolean useCustom);

  void rememberTrayNotificationSoundCustomPath(String relativePath);
}
