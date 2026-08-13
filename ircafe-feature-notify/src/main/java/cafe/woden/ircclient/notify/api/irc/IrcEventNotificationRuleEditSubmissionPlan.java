package cafe.woden.ircclient.notify.api.irc;

import java.util.Locale;
import java.util.Objects;

/** Feature-owned normalized values produced by the IRC-event notification rule editor. */
public record IrcEventNotificationRuleEditSubmissionPlan(
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
  public static final String CTCP_MATCH_ANY = "ANY";

  public IrcEventNotificationRuleEditSubmissionPlan {
    eventType = normalizeMode(eventType);
    sourceMode = normalizeMode(sourceMode);
    sourcePattern = normalizedText(sourcePattern);
    channelScope = normalizeMode(channelScope);
    channelPatterns = normalizedText(channelPatterns);
    ctcpCommandMode = normalizeModeWithFallback(ctcpCommandMode, CTCP_MATCH_ANY);
    ctcpCommandPattern = normalizedText(ctcpCommandPattern);
    ctcpValueMode = normalizeModeWithFallback(ctcpValueMode, CTCP_MATCH_ANY);
    ctcpValuePattern = normalizedText(ctcpValuePattern);
    soundCustomPath = normalizedText(soundCustomPath);
    soundUseCustom = soundUseCustom && soundCustomPath != null;
    scriptPath = normalizedText(scriptPath);
    scriptArgs = normalizedText(scriptArgs);
    scriptWorkingDirectory = normalizedText(scriptWorkingDirectory);
    scriptEnabled = scriptEnabled && scriptPath != null;

    if (!IrcEventNotificationRuleEditPolicy.sourcePatternRequired(sourceMode)) {
      sourcePattern = null;
    }
    if (!IrcEventNotificationRuleEditPolicy.channelPatternsRequired(channelScope)) {
      channelPatterns = null;
    }
    if (!IrcEventNotificationRuleEditPolicy.ctcpFiltersActive(eventType)) {
      ctcpCommandMode = CTCP_MATCH_ANY;
      ctcpCommandPattern = null;
      ctcpValueMode = CTCP_MATCH_ANY;
      ctcpValuePattern = null;
    } else {
      if (!IrcEventNotificationRuleEditPolicy.ctcpPatternRequired(ctcpCommandMode)) {
        ctcpCommandPattern = null;
      }
      if (!IrcEventNotificationRuleEditPolicy.ctcpPatternRequired(ctcpValueMode)) {
        ctcpValuePattern = null;
      }
    }
  }

  private static String normalizeMode(String value) {
    return Objects.toString(value, "").trim().toUpperCase(Locale.ROOT);
  }

  private static String normalizeModeWithFallback(String value, String fallback) {
    String normalized = normalizeMode(value);
    return normalized.isEmpty() ? fallback : normalized;
  }

  private static String normalizedText(String value) {
    String normalized = Objects.toString(value, "").trim();
    return normalized.isEmpty() ? null : normalized;
  }
}
