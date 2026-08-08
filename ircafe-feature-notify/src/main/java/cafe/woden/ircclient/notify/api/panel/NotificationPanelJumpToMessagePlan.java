package cafe.woden.ircclient.notify.api.panel;

/** Planned jump-to-message callback values for the notifications panel. */
public record NotificationPanelJumpToMessagePlan(boolean jump, String messageId) {
  public NotificationPanelJumpToMessagePlan {
    messageId = messageId == null ? "" : messageId.trim();
    if (!jump || messageId.isEmpty()) {
      jump = false;
      messageId = "";
    }
  }

  public static NotificationPanelJumpToMessagePlan skip() {
    return new NotificationPanelJumpToMessagePlan(false, "");
  }

  public static NotificationPanelJumpToMessagePlan jump(String messageId) {
    return new NotificationPanelJumpToMessagePlan(true, messageId);
  }
}
