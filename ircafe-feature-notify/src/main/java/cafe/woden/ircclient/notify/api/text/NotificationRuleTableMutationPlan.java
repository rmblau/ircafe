package cafe.woden.ircclient.notify.api.text;

/** Feature-safe plan for a notification-rule table mutation and its selection result. */
public record NotificationRuleTableMutationPlan(
    boolean proceed, int row, int targetRow, int rowToSelect, boolean clearSelection) {
  public static final int NO_ROW = -1;

  public static NotificationRuleTableMutationPlan skip() {
    return new NotificationRuleTableMutationPlan(false, NO_ROW, NO_ROW, NO_ROW, false);
  }

  static NotificationRuleTableMutationPlan select(int row, int targetRow, int rowToSelect) {
    return new NotificationRuleTableMutationPlan(true, row, targetRow, rowToSelect, false);
  }

  static NotificationRuleTableMutationPlan clear(int row, int targetRow) {
    return new NotificationRuleTableMutationPlan(true, row, targetRow, NO_ROW, true);
  }

  public boolean selectRow() {
    return rowToSelect >= 0;
  }
}
