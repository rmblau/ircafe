package cafe.woden.ircclient.notify.api.sound;

/** Feature-owned policy for clearing custom notification sound selection state. */
public final class NotificationSoundClearSelectionPlanner {
  private NotificationSoundClearSelectionPlanner() {}

  public static NotificationSoundClearSelectionPlan plan() {
    return NotificationSoundClearSelectionPlan.cleared();
  }
}
