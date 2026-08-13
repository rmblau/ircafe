package cafe.woden.ircclient.notify.api.panel;

/** Feature-owned jump-to-message preflight for the notifications panel. */
public final class NotificationPanelJumpToMessagePlanner {
  private NotificationPanelJumpToMessagePlanner() {}

  public static NotificationPanelJumpToMessagePlan plan(boolean targetValid, String messageId) {
    if (!targetValid) return NotificationPanelJumpToMessagePlan.skip();
    return NotificationPanelJumpToMessagePlan.jump(messageId);
  }
}
