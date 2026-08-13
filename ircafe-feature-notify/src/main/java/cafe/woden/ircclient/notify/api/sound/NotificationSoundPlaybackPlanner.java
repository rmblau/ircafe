package cafe.woden.ircclient.notify.api.sound;

/** Feature-owned custom-vs-built-in notification sound selection policy. */
public final class NotificationSoundPlaybackPlanner {
  private NotificationSoundPlaybackPlanner() {}

  public static NotificationSoundPlaybackPlan planSelected(
      boolean soundsEnabled,
      boolean customEnabled,
      boolean customAvailable,
      String builtInResourcePath) {
    if (!soundsEnabled) {
      return NotificationSoundPlaybackPlan.skip();
    }
    return select(customEnabled, customAvailable, builtInResourcePath);
  }

  public static NotificationSoundPlaybackPlan planOverride(
      boolean soundsEnabled,
      boolean customRequested,
      boolean customAvailable,
      String builtInResourcePath) {
    if (!soundsEnabled) {
      return NotificationSoundPlaybackPlan.skip();
    }
    return select(customRequested, customAvailable, builtInResourcePath);
  }

  public static NotificationSoundPlaybackPlan planBuiltInPreview(String builtInResourcePath) {
    return NotificationSoundPlaybackPlan.builtInResource(builtInResourcePath);
  }

  public static NotificationSoundPlaybackPlan planCustomPreview(boolean customAvailable) {
    return customAvailable
        ? NotificationSoundPlaybackPlan.customFile()
        : NotificationSoundPlaybackPlan.skip();
  }

  private static NotificationSoundPlaybackPlan select(
      boolean customRequested, boolean customAvailable, String builtInResourcePath) {
    if (customRequested && customAvailable) {
      return NotificationSoundPlaybackPlan.customFile();
    }
    return NotificationSoundPlaybackPlan.builtInResource(builtInResourcePath);
  }
}
