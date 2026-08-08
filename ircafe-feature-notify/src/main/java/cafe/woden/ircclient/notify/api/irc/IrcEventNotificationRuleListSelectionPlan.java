package cafe.woden.ircclient.notify.api.irc;

/** Feature-safe table selection decision after IRC-event rule-list replacement operations. */
public record IrcEventNotificationRuleListSelectionPlan(boolean selectRow, int row) {
  public IrcEventNotificationRuleListSelectionPlan {
    if (!selectRow) {
      row = -1;
    }
  }
}
