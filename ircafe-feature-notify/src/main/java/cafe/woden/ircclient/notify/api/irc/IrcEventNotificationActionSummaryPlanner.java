package cafe.woden.ircclient.notify.api.irc;

/** Plans IRC-event notification action summaries without Swing/localized labels. */
public final class IrcEventNotificationActionSummaryPlanner {
  private IrcEventNotificationActionSummaryPlanner() {}

  public static IrcEventNotificationActionSummaryPlan plan(IrcEventNotificationActionRule rule) {
    if (rule == null) return IrcEventNotificationActionSummaryPlan.none();

    IrcEventNotificationActionPlan actionPlan = IrcEventNotificationActionPlanner.plan(rule);
    IrcEventNotificationTrayAction trayAction = actionPlan.trayAction();
    IrcEventNotificationScriptAction scriptAction = actionPlan.scriptAction();

    return new IrcEventNotificationActionSummaryPlan(
        trayAction.showToast(),
        trayAction.focusScope(),
        trayAction.showStatusBar(),
        actionPlan.recordNotification(),
        trayAction.playSound(),
        trayAction.soundUseCustom(),
        trayAction.soundId(),
        scriptAction.enabled(),
        scriptAction.scriptPath());
  }
}
