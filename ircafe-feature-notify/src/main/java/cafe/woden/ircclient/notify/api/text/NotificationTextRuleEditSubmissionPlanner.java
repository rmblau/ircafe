package cafe.woden.ircclient.notify.api.text;

/** Normalizes plain notification text-rule editor values before root adapts them to config. */
public final class NotificationTextRuleEditSubmissionPlanner {
  private NotificationTextRuleEditSubmissionPlanner() {}

  public static NotificationTextRuleEditSubmissionPlan plan(
      String label,
      NotificationTextRule.Type type,
      String pattern,
      boolean enabled,
      boolean caseSensitive,
      boolean wholeWord,
      String highlightFg) {
    return new NotificationTextRuleEditSubmissionPlan(
        label, type, pattern, enabled, caseSensitive, wholeWord, highlightFg);
  }
}
