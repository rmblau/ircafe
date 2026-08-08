package cafe.woden.ircclient.notify.api.text;

/** Plans plain notification text-rule edit-control state without depending on Swing. */
public final class NotificationTextRuleEditFieldPlanner {
  private NotificationTextRuleEditFieldPlanner() {}

  public static NotificationTextRuleEditFieldPlan plan(
      NotificationTextRule.Type type, boolean wholeWordSelected) {
    boolean wholeWordAvailable = NotificationTextRuleEditPolicy.wholeWordOptionAvailable(type);
    return new NotificationTextRuleEditFieldPlan(
        wholeWordAvailable,
        NotificationTextRuleEditPolicy.normalizeWholeWord(type, wholeWordSelected));
  }
}
