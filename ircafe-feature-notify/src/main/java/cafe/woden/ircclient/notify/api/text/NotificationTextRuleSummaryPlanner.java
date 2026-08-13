package cafe.woden.ircclient.notify.api.text;

/** Plans normalized, feature-owned summary values for notification text rules. */
public final class NotificationTextRuleSummaryPlanner {
  private NotificationTextRuleSummaryPlanner() {}

  public static NotificationTextRuleSummaryPlan plan(NotificationTextRule rule) {
    if (rule == null) {
      return new NotificationTextRuleSummaryPlan(
          "", NotificationTextRule.Type.WORD, "", false, true);
    }
    return new NotificationTextRuleSummaryPlan(
        rule.label(), rule.type(), rule.pattern(), rule.caseSensitive(), rule.wholeWord());
  }
}
