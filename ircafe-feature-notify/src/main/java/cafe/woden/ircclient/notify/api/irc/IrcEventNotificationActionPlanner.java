package cafe.woden.ircclient.notify.api.irc;

/** Feature-owned action planning for matched IRC event notification rules. */
public final class IrcEventNotificationActionPlanner {
  private IrcEventNotificationActionPlanner() {}

  public static IrcEventNotificationActionPlan plan(IrcEventNotificationActionRule rule) {
    if (rule == null) return IrcEventNotificationActionPlan.none();

    boolean notifyTray = rule.toastEnabled() || rule.statusBarEnabled() || rule.soundEnabled();
    IrcEventNotificationTrayAction trayAction =
        notifyTray
            ? new IrcEventNotificationTrayAction(
                true,
                rule.toastEnabled(),
                rule.statusBarEnabled(),
                rule.focusScope(),
                rule.soundEnabled(),
                rule.soundId(),
                rule.soundUseCustom(),
                rule.soundCustomPath())
            : IrcEventNotificationTrayAction.disabled();

    IrcEventNotificationScriptAction scriptAction =
        IrcEventNotificationScriptAction.of(
            rule.scriptEnabled(),
            rule.scriptPath(),
            rule.scriptArgs(),
            rule.scriptWorkingDirectory());

    return new IrcEventNotificationActionPlan(
        rule.notificationsNodeEnabled(), trayAction, scriptAction, true);
  }
}
