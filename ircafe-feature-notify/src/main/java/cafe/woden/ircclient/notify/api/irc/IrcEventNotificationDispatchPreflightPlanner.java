package cafe.woden.ircclient.notify.api.irc;

/** Plans pure preflight checks before root dispatches IRC-event notification side effects. */
public final class IrcEventNotificationDispatchPreflightPlanner {
  private IrcEventNotificationDispatchPreflightPlanner() {}

  public static IrcEventNotificationDispatchPreflightPlan plan(
      String eventTypeName, int ruleCount) {
    return new IrcEventNotificationDispatchPreflightPlan(true, eventTypeName, ruleCount);
  }
}
