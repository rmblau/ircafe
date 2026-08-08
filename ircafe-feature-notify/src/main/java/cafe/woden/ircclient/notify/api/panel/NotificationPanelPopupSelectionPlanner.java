package cafe.woden.ircclient.notify.api.panel;

/** Feature-owned popup-menu selection preflight for the notifications panel. */
public final class NotificationPanelPopupSelectionPlanner {
  private NotificationPanelPopupSelectionPlanner() {}

  public static NotificationPanelPopupSelectionPlan plan(
      boolean popupTrigger, int viewRow, boolean rowAlreadySelected) {
    if (!popupTrigger) return NotificationPanelPopupSelectionPlan.skip();
    if (viewRow < 0) return NotificationPanelPopupSelectionPlan.clear();
    if (rowAlreadySelected) return NotificationPanelPopupSelectionPlan.keepSelection();
    return NotificationPanelPopupSelectionPlan.select(viewRow);
  }
}
