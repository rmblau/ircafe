package cafe.woden.ircclient.notify.api.sound;

/** Feature-owned custom-vs-built-in preview policy for notification sound controls. */
public final class NotificationSoundPreviewPlanner {
  private NotificationSoundPreviewPlanner() {}

  public static NotificationSoundPreviewPlan plan(boolean useCustomSelected, String customPath) {
    if (useCustomSelected) {
      return NotificationSoundPreviewPlan.customFile(customPath);
    }
    return NotificationSoundPreviewPlan.builtInSound();
  }
}
