package cafe.woden.ircclient.notify.api.irc;

/** Normalizes IRC-event notification rule editor values before root adapts them to config. */
public final class IrcEventNotificationRuleEditSubmissionPlanner {
  private IrcEventNotificationRuleEditSubmissionPlanner() {}

  public static IrcEventNotificationRuleEditSubmissionPlan plan(
      String eventType,
      String sourceMode,
      String sourcePattern,
      String channelScope,
      String channelPatterns,
      String ctcpCommandMode,
      String ctcpCommandPattern,
      String ctcpValueMode,
      String ctcpValuePattern,
      boolean soundUseCustom,
      String soundCustomPath,
      boolean scriptEnabled,
      String scriptPath,
      String scriptArgs,
      String scriptWorkingDirectory) {
    return new IrcEventNotificationRuleEditSubmissionPlan(
        eventType,
        sourceMode,
        sourcePattern,
        channelScope,
        channelPatterns,
        ctcpCommandMode,
        ctcpCommandPattern,
        ctcpValueMode,
        ctcpValuePattern,
        soundUseCustom,
        soundCustomPath,
        scriptEnabled,
        scriptPath,
        scriptArgs,
        scriptWorkingDirectory);
  }
}
