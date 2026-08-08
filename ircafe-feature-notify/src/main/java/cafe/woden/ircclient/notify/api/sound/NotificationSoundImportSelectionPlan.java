package cafe.woden.ircclient.notify.api.sound;

import java.util.Objects;

/** Feature-safe outcome for applying an imported custom notification sound path to controls. */
public record NotificationSoundImportSelectionPlan(boolean applyCustomSound, String customPath) {

  public NotificationSoundImportSelectionPlan {
    customPath = Objects.toString(customPath, "").trim();
    if (customPath.isEmpty()) {
      customPath = null;
      applyCustomSound = false;
    }
  }

  public static NotificationSoundImportSelectionPlan skip() {
    return new NotificationSoundImportSelectionPlan(false, null);
  }

  public static NotificationSoundImportSelectionPlan customSound(String customPath) {
    return new NotificationSoundImportSelectionPlan(true, customPath);
  }
}
