package cafe.woden.ircclient.notify.api.text;

/** Plans notification rule table action availability without Swing dependencies. */
public final class NotificationRuleTableSelectionPlanner {
  private NotificationRuleTableSelectionPlanner() {}

  public static NotificationRuleTableSelectionPlan plan(int selectedRow, int rowCount) {
    int count = Math.max(0, rowCount);
    if (selectedRow < 0 || selectedRow >= count) return NotificationRuleTableSelectionPlan.none();

    return new NotificationRuleTableSelectionPlan(
        true, true, true, selectedRow > 0, selectedRow < count - 1);
  }
}
