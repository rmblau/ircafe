package cafe.woden.ircclient.notify.api.irc;

/** Pure display-bounding policy for IRC-event notification table summaries. */
public final class IrcEventNotificationTableSummaryDisplayPlanner {
  public static final int SOURCE_PATTERN_MAX_CHARS = 56;
  public static final int CHANNEL_PATTERNS_MAX_CHARS = 56;
  public static final int CTCP_PATTERN_MAX_CHARS = 24;
  public static final int SCRIPT_LEAF_NAME_MAX_CHARS = 26;

  private IrcEventNotificationTableSummaryDisplayPlanner() {}

  public static IrcEventNotificationTableSummaryDisplayPlan plan(
      IrcEventNotificationMatchSummaryPlan matchSummary,
      IrcEventNotificationActionSummaryPlan actionSummary) {
    return new IrcEventNotificationTableSummaryDisplayPlan(
        bounded(
            matchSummary != null ? matchSummary.sourcePattern() : null, SOURCE_PATTERN_MAX_CHARS),
        bounded(
            matchSummary != null ? matchSummary.channelPatterns() : null,
            CHANNEL_PATTERNS_MAX_CHARS),
        bounded(
            matchSummary != null ? matchSummary.ctcpCommandPattern() : null,
            CTCP_PATTERN_MAX_CHARS),
        bounded(
            matchSummary != null ? matchSummary.ctcpValuePattern() : null, CTCP_PATTERN_MAX_CHARS),
        bounded(
            actionSummary != null ? actionSummary.scriptLeafName() : null,
            SCRIPT_LEAF_NAME_MAX_CHARS));
  }

  public static String bounded(String value, int maxChars) {
    if (value == null) return null;
    String normalized = value.trim();
    if (normalized.isEmpty()) return null;
    if (maxChars <= 0) return "";
    if (normalized.length() <= maxChars) return normalized;
    if (maxChars <= 1) return "…";
    return normalized.substring(0, maxChars - 1) + "…";
  }
}
