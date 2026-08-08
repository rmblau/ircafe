package cafe.woden.ircclient.notify.api.sound;

/** Feature-owned policy for applying a successfully imported custom notification sound path. */
public final class NotificationSoundImportSelectionPlanner {
  private NotificationSoundImportSelectionPlanner() {}

  public static NotificationSoundImportSelectionPlan plan(String importedRelativePath) {
    return NotificationSoundImportSelectionPlan.customSound(importedRelativePath);
  }
}
