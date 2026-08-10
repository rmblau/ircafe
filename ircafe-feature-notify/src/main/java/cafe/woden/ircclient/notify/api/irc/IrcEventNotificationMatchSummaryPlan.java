package cafe.woden.ircclient.notify.api.irc;

import java.util.Locale;
import java.util.Objects;

/** Feature-owned normalized IRC-event match summary values for UI/root adapters. */
public record IrcEventNotificationMatchSummaryPlan(
    String sourceMode,
    boolean sourcePatternRequired,
    String sourcePattern,
    String channelScope,
    boolean channelPatternsRequired,
    String channelPatterns,
    boolean ctcpFiltersActive,
    String ctcpCommandMode,
    boolean ctcpCommandPatternRequired,
    String ctcpCommandPattern,
    String ctcpValueMode,
    boolean ctcpValuePatternRequired,
    String ctcpValuePattern) {

  public IrcEventNotificationMatchSummaryPlan {
    sourceMode = defaultMode(sourceMode, "ANY");
    sourcePatternRequired = IrcEventNotificationRuleEditPolicy.sourcePatternRequired(sourceMode);
    sourcePattern = trimToNull(sourcePattern);
    if (!sourcePatternRequired) {
      sourcePattern = null;
    }

    channelScope = defaultMode(channelScope, "ALL");
    channelPatternsRequired =
        IrcEventNotificationRuleEditPolicy.channelPatternsRequired(channelScope);
    channelPatterns = trimToNull(channelPatterns);
    if (!channelPatternsRequired) {
      channelPatterns = null;
    }

    if (!ctcpFiltersActive) {
      ctcpCommandMode = "ANY";
      ctcpCommandPatternRequired = false;
      ctcpCommandPattern = null;
      ctcpValueMode = "ANY";
      ctcpValuePatternRequired = false;
      ctcpValuePattern = null;
    } else {
      ctcpCommandMode = defaultMode(ctcpCommandMode, "ANY");
      ctcpCommandPatternRequired =
          IrcEventNotificationRuleEditPolicy.ctcpPatternRequired(ctcpCommandMode);
      ctcpCommandPattern = trimToNull(ctcpCommandPattern);
      if (!ctcpCommandPatternRequired) {
        ctcpCommandPattern = null;
      }

      ctcpValueMode = defaultMode(ctcpValueMode, "ANY");
      ctcpValuePatternRequired =
          IrcEventNotificationRuleEditPolicy.ctcpPatternRequired(ctcpValueMode);
      ctcpValuePattern = trimToNull(ctcpValuePattern);
      if (!ctcpValuePatternRequired) {
        ctcpValuePattern = null;
      }
    }
  }

  static IrcEventNotificationMatchSummaryPlan none() {
    return new IrcEventNotificationMatchSummaryPlan(
        "ANY", false, null, "ALL", false, null, false, "ANY", false, null, "ANY", false, null);
  }

  private static String defaultMode(String raw, String fallback) {
    String value = Objects.toString(raw, "").trim();
    return value.isEmpty() ? fallback : value.toUpperCase(Locale.ROOT);
  }

  private static String trimToNull(String raw) {
    String value = Objects.toString(raw, "").trim();
    return value.isEmpty() ? null : value;
  }
}
