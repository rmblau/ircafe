package cafe.woden.ircclient.notify.api.irc;

/** Feature-safe side-effect plan for a matched IRC event notification rule. */
public record IrcEventNotificationActionPlan(
    boolean recordNotification,
    IrcEventNotificationTrayAction trayAction,
    IrcEventNotificationScriptAction scriptAction,
    boolean sendPush) {

  public IrcEventNotificationActionPlan {
    trayAction = trayAction != null ? trayAction : IrcEventNotificationTrayAction.disabled();
    scriptAction =
        scriptAction != null ? scriptAction : IrcEventNotificationScriptAction.disabled();
  }

  public boolean runScript() {
    return scriptAction.enabled();
  }

  public static IrcEventNotificationActionPlan none() {
    return new IrcEventNotificationActionPlan(
        false,
        IrcEventNotificationTrayAction.disabled(),
        IrcEventNotificationScriptAction.disabled(),
        false);
  }
}
