package cafe.woden.ircclient.notify.api.irc;

/** Feature-owned UI availability plan for editing an IRC-event notification rule. */
public record IrcEventNotificationRuleEditFieldPlan(
    SourcePatternHint sourcePatternHint,
    boolean sourcePatternAvailable,
    boolean channelPatternsAvailable,
    boolean ctcpFiltersAvailable,
    boolean ctcpCommandPatternAvailable,
    boolean ctcpValuePatternAvailable,
    boolean scriptFieldsAvailable) {

  public IrcEventNotificationRuleEditFieldPlan {
    sourcePatternHint = sourcePatternHint == null ? SourcePatternHint.NONE : sourcePatternHint;
  }

  public enum SourcePatternHint {
    NONE,
    NICK_LIST,
    GLOB,
    REGEX
  }
}
