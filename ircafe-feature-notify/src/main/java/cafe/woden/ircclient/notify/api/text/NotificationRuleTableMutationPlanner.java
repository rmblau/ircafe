package cafe.woden.ircclient.notify.api.text;

/** Plans notification-rule table mutation preflight and post-mutation selection without Swing. */
public final class NotificationRuleTableMutationPlanner {
  private NotificationRuleTableMutationPlanner() {}

  public static NotificationRuleTableMutationPlan selectedRow(int selectedRow, int rowCount) {
    int count = normalizedCount(rowCount);
    if (!validRow(selectedRow, count)) return NotificationRuleTableMutationPlan.skip();
    return NotificationRuleTableMutationPlan.select(selectedRow, selectedRow, selectedRow);
  }

  public static NotificationRuleTableMutationPlan afterMutation(int rowToSelect, int rowCount) {
    int count = normalizedCount(rowCount);
    if (!validRow(rowToSelect, count)) return NotificationRuleTableMutationPlan.skip();
    return NotificationRuleTableMutationPlan.select(rowToSelect, rowToSelect, rowToSelect);
  }

  public static NotificationRuleTableMutationPlan move(
      int selectedRow, int rowCount, int targetOffset) {
    int count = normalizedCount(rowCount);
    if (!validRow(selectedRow, count)) return NotificationRuleTableMutationPlan.skip();

    int targetRow = selectedRow + targetOffset;
    if (!validRow(targetRow, count)) return NotificationRuleTableMutationPlan.skip();

    return NotificationRuleTableMutationPlan.select(selectedRow, targetRow, targetRow);
  }

  public static NotificationRuleTableMutationPlan afterRemoval(
      int removedRow, int remainingRowCount) {
    if (removedRow < 0) return NotificationRuleTableMutationPlan.skip();

    int count = normalizedCount(remainingRowCount);
    if (count <= 0) return NotificationRuleTableMutationPlan.clear(removedRow, -1);

    int nextRow = Math.min(removedRow, count - 1);
    return NotificationRuleTableMutationPlan.select(removedRow, -1, nextRow);
  }

  private static int normalizedCount(int rowCount) {
    return Math.max(0, rowCount);
  }

  private static boolean validRow(int row, int rowCount) {
    return row >= 0 && row < rowCount;
  }
}
