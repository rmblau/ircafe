package cafe.woden.ircclient.notify.sound;

import cafe.woden.ircclient.model.BuiltInSound;
import cafe.woden.ircclient.notify.api.sound.NotificationSoundSettingsPolicy;
import cafe.woden.ircclient.notify.api.sound.NotificationSoundSettingsValues;

/**
 * Notification sound preferences.
 *
 * <p>Phase 3: a single global sound with a global enable toggle.
 */
public record NotificationSoundSettings(
    boolean enabled, String soundId, boolean useCustom, String customPath) {

  public NotificationSoundSettings {
    NotificationSoundSettingsValues normalized =
        NotificationSoundSettingsPolicy.normalize(
            enabled, soundId, useCustom, customPath, BuiltInSound.NOTIF_1.name());
    enabled = normalized.enabled();
    soundId = normalized.soundId();
    useCustom = normalized.useCustom();
    customPath = normalized.customPath();
  }
}
