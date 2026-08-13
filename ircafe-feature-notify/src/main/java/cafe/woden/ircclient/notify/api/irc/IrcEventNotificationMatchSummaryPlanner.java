package cafe.woden.ircclient.notify.api.irc;

/** Plans normalized IRC-event match summaries without Swing or localized labels. */
public final class IrcEventNotificationMatchSummaryPlanner {
  private IrcEventNotificationMatchSummaryPlanner() {}

  public static IrcEventNotificationMatchSummaryPlan plan(IrcEventNotificationMatchRule rule) {
    if (rule == null) return IrcEventNotificationMatchSummaryPlan.none();

    boolean sourcePatternRequired =
        IrcEventNotificationRuleEditPolicy.sourcePatternRequired(rule.sourceMode());
    boolean channelPatternsRequired =
        IrcEventNotificationRuleEditPolicy.channelPatternsRequired(rule.channelScope());
    boolean ctcpFiltersActive =
        IrcEventNotificationRuleEditPolicy.ctcpFiltersActive(rule.eventType());
    boolean ctcpCommandPatternRequired =
        ctcpFiltersActive
            && IrcEventNotificationRuleEditPolicy.ctcpPatternRequired(rule.ctcpCommandMode());
    boolean ctcpValuePatternRequired =
        ctcpFiltersActive
            && IrcEventNotificationRuleEditPolicy.ctcpPatternRequired(rule.ctcpValueMode());

    return new IrcEventNotificationMatchSummaryPlan(
        rule.sourceMode(),
        sourcePatternRequired,
        rule.sourcePattern(),
        rule.channelScope(),
        channelPatternsRequired,
        rule.channelPatterns(),
        ctcpFiltersActive,
        rule.ctcpCommandMode(),
        ctcpCommandPatternRequired,
        rule.ctcpCommandPattern(),
        rule.ctcpValueMode(),
        ctcpValuePatternRequired,
        rule.ctcpValuePattern());
  }
}
