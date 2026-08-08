package cafe.woden.ircclient.notify.api.irc;

/** Feature-owned routing policy for IRC-event notification rule-edit validation display. */
public final class IrcEventNotificationRuleEditValidationDisplayPlanner {
  public static final int FILTERS_TAB_INDEX = 0;
  public static final int SCRIPT_TAB_INDEX = 3;

  private IrcEventNotificationRuleEditValidationDisplayPlanner() {}

  public static IrcEventNotificationRuleEditValidationDisplayPlan plan(
      IrcEventNotificationRuleEditValidationError error) {
    int tabIndex =
        error != null
            && error.field() == IrcEventNotificationRuleEditValidationError.Field.SCRIPT_PATH
            ? SCRIPT_TAB_INDEX
            : FILTERS_TAB_INDEX;
    return new IrcEventNotificationRuleEditValidationDisplayPlan(tabIndex);
  }
}
