package cafe.woden.ircclient.notify.api.panel;

/** Planned selection adjustment before showing the notifications panel row menu. */
public record NotificationPanelPopupSelectionPlan(
    boolean showMenu, boolean selectRow, int rowToSelect, boolean clearSelection) {
  public NotificationPanelPopupSelectionPlan {
    if (!showMenu) {
      selectRow = false;
      rowToSelect = -1;
      clearSelection = false;
    } else if (selectRow && rowToSelect < 0) {
      selectRow = false;
    }
  }

  public static NotificationPanelPopupSelectionPlan skip() {
    return new NotificationPanelPopupSelectionPlan(false, false, -1, false);
  }

  public static NotificationPanelPopupSelectionPlan keepSelection() {
    return new NotificationPanelPopupSelectionPlan(true, false, -1, false);
  }

  public static NotificationPanelPopupSelectionPlan select(int row) {
    return new NotificationPanelPopupSelectionPlan(true, true, row, false);
  }

  public static NotificationPanelPopupSelectionPlan clear() {
    return new NotificationPanelPopupSelectionPlan(true, false, -1, true);
  }
}
