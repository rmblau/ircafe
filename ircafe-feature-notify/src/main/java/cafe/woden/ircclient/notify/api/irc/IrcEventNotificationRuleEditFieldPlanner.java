package cafe.woden.ircclient.notify.api.irc;

import java.util.Locale;
import java.util.Objects;

/** Plans IRC-event notification rule edit-control state without depending on Swing. */
public final class IrcEventNotificationRuleEditFieldPlanner {
  private IrcEventNotificationRuleEditFieldPlanner() {}

  public static IrcEventNotificationRuleEditFieldPlan plan(
      String eventType,
      String sourceMode,
      String channelScope,
      String ctcpCommandMode,
      String ctcpValueMode,
      boolean scriptEnabled) {
    boolean sourcePatternAvailable =
        IrcEventNotificationRuleEditPolicy.sourcePatternRequired(sourceMode);
    boolean channelPatternsAvailable =
        IrcEventNotificationRuleEditPolicy.channelPatternsRequired(channelScope);
    boolean ctcpFiltersAvailable = IrcEventNotificationRuleEditPolicy.ctcpFiltersActive(eventType);
    boolean ctcpCommandPatternAvailable =
        ctcpFiltersAvailable
            && IrcEventNotificationRuleEditPolicy.ctcpPatternRequired(ctcpCommandMode);
    boolean ctcpValuePatternAvailable =
        ctcpFiltersAvailable
            && IrcEventNotificationRuleEditPolicy.ctcpPatternRequired(ctcpValueMode);

    return new IrcEventNotificationRuleEditFieldPlan(
        sourcePatternHint(sourceMode),
        sourcePatternAvailable,
        channelPatternsAvailable,
        ctcpFiltersAvailable,
        ctcpCommandPatternAvailable,
        ctcpValuePatternAvailable,
        scriptEnabled);
  }

  private static IrcEventNotificationRuleEditFieldPlan.SourcePatternHint sourcePatternHint(
      String sourceMode) {
    return switch (normalize(sourceMode)) {
      case "NICK_LIST" -> IrcEventNotificationRuleEditFieldPlan.SourcePatternHint.NICK_LIST;
      case "GLOB" -> IrcEventNotificationRuleEditFieldPlan.SourcePatternHint.GLOB;
      case "REGEX" -> IrcEventNotificationRuleEditFieldPlan.SourcePatternHint.REGEX;
      default -> IrcEventNotificationRuleEditFieldPlan.SourcePatternHint.NONE;
    };
  }

  private static String normalize(String value) {
    return Objects.toString(value, "").trim().toUpperCase(Locale.ROOT);
  }
}
