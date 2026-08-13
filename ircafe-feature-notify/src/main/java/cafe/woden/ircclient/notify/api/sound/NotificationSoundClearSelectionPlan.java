package cafe.woden.ircclient.notify.api.sound;

import java.util.Objects;

/** Feature-owned selection values to apply when a custom notification sound is cleared. */
public record NotificationSoundClearSelectionPlan(boolean useCustomSelected, String customPath) {
  public NotificationSoundClearSelectionPlan {
    customPath = Objects.toString(customPath, "");
  }

  public static NotificationSoundClearSelectionPlan cleared() {
    return new NotificationSoundClearSelectionPlan(false, "");
  }
}
