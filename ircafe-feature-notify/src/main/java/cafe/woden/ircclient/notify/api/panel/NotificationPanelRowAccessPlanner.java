package cafe.woden.ircclient.notify.api.panel;

/** Feature-owned bounds checks for notification panel row access decisions. */
public final class NotificationPanelRowAccessPlanner {
  private NotificationPanelRowAccessPlanner() {}

  public static NotificationPanelRowAccessPlan rowAtView(int viewRow, int rowCount) {
    return new NotificationPanelRowAccessPlan(viewRow >= 0 && viewRow < rowCount, viewRow);
  }

  public static NotificationPanelRowAccessPlan selectedSingleRow(
      int selectedRowCount, int selectedViewRow, int rowCount) {
    if (selectedRowCount != 1) return new NotificationPanelRowAccessPlan(false, -1);
    return rowAtView(selectedViewRow, rowCount);
  }
}
