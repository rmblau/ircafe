package cafe.woden.ircclient.notify.api.irc;

import cafe.woden.ircclient.notify.api.text.NotificationRuleTableSelectionPlan;
import cafe.woden.ircclient.notify.api.text.NotificationRuleTableSelectionPlanner;

/** Plans IRC-event notification enable/disable action availability without Swing dependencies. */
public final class IrcEventNotificationRuleToggleSelectionPlanner {
  private IrcEventNotificationRuleToggleSelectionPlanner() {}

  public static IrcEventNotificationRuleToggleSelectionPlan plan(
      int selectedRow, int rowCount, boolean selectedRuleEnabled) {
    NotificationRuleTableSelectionPlan selection =
        NotificationRuleTableSelectionPlanner.plan(selectedRow, rowCount);
    boolean hasSelection = selection.editEnabled();
    if (!hasSelection) return IrcEventNotificationRuleToggleSelectionPlan.none();

    return new IrcEventNotificationRuleToggleSelectionPlan(
        !selectedRuleEnabled, selectedRuleEnabled);
  }
}
