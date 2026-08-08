package cafe.woden.ircclient.notify.api.irc;

/** Plans table selection behavior after IRC-event notification rule-list replacement. */
public final class IrcEventNotificationRuleListSelectionPlanner {
  private IrcEventNotificationRuleListSelectionPlanner() {}

  public static IrcEventNotificationRuleListSelectionPlan afterPresetApply(
      int rowCount, int preferredRow) {
    if (rowCount <= 0) {
      return new IrcEventNotificationRuleListSelectionPlan(false, -1);
    }
    int row = preferredRow >= 0 && preferredRow < rowCount ? preferredRow : 0;
    return new IrcEventNotificationRuleListSelectionPlan(true, row);
  }

  public static IrcEventNotificationRuleListSelectionPlan afterDefaultReset(int rowCount) {
    if (rowCount <= 0) {
      return new IrcEventNotificationRuleListSelectionPlan(false, -1);
    }
    return new IrcEventNotificationRuleListSelectionPlan(true, 0);
  }
}
