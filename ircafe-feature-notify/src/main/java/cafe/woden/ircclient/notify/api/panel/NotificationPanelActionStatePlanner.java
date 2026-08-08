package cafe.woden.ircclient.notify.api.panel;

import java.util.Objects;

/** Feature-owned action availability rules for the notification panel. */
public final class NotificationPanelActionStatePlanner {
  private NotificationPanelActionStatePlanner() {}

  public static NotificationPanelActionStatePlan plan(
      int rowCount,
      int selectedRowCount,
      String selectedMessageId,
      boolean selectedTargetAvailable) {
    boolean hasRows = rowCount > 0;
    boolean hasSelection = selectedRowCount > 0;
    boolean singleSelection = selectedRowCount == 1;
    boolean hasMessageId = !Objects.toString(selectedMessageId, "").trim().isEmpty();
    boolean canJump = singleSelection && hasMessageId && selectedTargetAvailable;

    return new NotificationPanelActionStatePlan(
        canJump,
        hasSelection,
        hasRows,
        hasSelection,
        hasRows);
  }
}
