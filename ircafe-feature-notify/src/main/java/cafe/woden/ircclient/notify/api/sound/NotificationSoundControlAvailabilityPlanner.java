package cafe.woden.ircclient.notify.api.sound;

import java.util.Objects;

/** Feature-owned state policy for notification sound control availability. */
public final class NotificationSoundControlAvailabilityPlanner {
  private NotificationSoundControlAvailabilityPlanner() {}

  public static NotificationSoundControlAvailabilityPlan plan(
      boolean available,
      boolean soundSelected,
      boolean useCustomSelected,
      String customPath,
      boolean customPathEditableWhenEnabled,
      boolean customFileControlsRequireUseCustom) {
    boolean soundOn = available && soundSelected;
    boolean customControlsEnabled =
        soundOn && (!customFileControlsRequireUseCustom || useCustomSelected);
    boolean customPathPresent = !Objects.toString(customPath, "").trim().isEmpty();

    return new NotificationSoundControlAvailabilityPlan(
        available,
        soundOn,
        soundOn && !useCustomSelected,
        customControlsEnabled,
        customControlsEnabled && customPathEditableWhenEnabled,
        customControlsEnabled,
        customControlsEnabled && customPathPresent,
        soundOn);
  }
}
