package cafe.woden.ircclient.notify.api.irc;

/** Feature-safe enabled-state plan for IRC-event notification rule toggle actions. */
public record IrcEventNotificationRuleToggleSelectionPlan(
    boolean enableRuleEnabled, boolean disableRuleEnabled) {

  public static IrcEventNotificationRuleToggleSelectionPlan none() {
    return new IrcEventNotificationRuleToggleSelectionPlan(false, false);
  }
}
