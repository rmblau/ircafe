package cafe.woden.ircclient.notify.api.irc;

import java.util.Locale;
import java.util.regex.Pattern;

/** Feature-owned edit-time validation for IRC-event notification rules. */
public final class IrcEventNotificationRuleEditPolicy {
  private static final String CTCP_RECEIVED = "CTCP_RECEIVED";
  private static final String ANY = "ANY";
  private static final String NICK_LIST = "NICK_LIST";
  private static final String GLOB = "GLOB";
  private static final String REGEX = "REGEX";
  private static final String ONLY = "ONLY";
  private static final String ALL_EXCEPT = "ALL_EXCEPT";

  private IrcEventNotificationRuleEditPolicy() {}

  public static IrcEventNotificationRuleEditValidationError validate(
      IrcEventNotificationRuleEditValues values) {
    if (values == null) return null;

    String sourceMode = mode(values.sourceMode(), ANY);
    if (sourceNeedsPattern(sourceMode)) {
      if (values.sourcePattern().isEmpty()) {
        return required(IrcEventNotificationRuleEditValidationError.Field.SOURCE_PATTERN);
      }
      if (REGEX.equals(sourceMode)) {
        IrcEventNotificationRuleEditValidationError error =
            regexError(
                IrcEventNotificationRuleEditValidationError.Field.SOURCE_PATTERN,
                values.sourcePattern());
        if (error != null) return error;
      }
    }

    String channelScope = mode(values.channelScope(), ANY);
    if (channelNeedsPattern(channelScope) && values.channelPatterns().isEmpty()) {
      return required(IrcEventNotificationRuleEditValidationError.Field.CHANNEL_PATTERNS);
    }

    if (!ctcpFiltersActive(values.eventType())) {
      return scriptPathError(values);
    }

    String commandMode = mode(values.ctcpCommandMode(), ANY);
    if (ctcpPatternRequired(commandMode)) {
      if (values.ctcpCommandPattern().isEmpty()) {
        return required(IrcEventNotificationRuleEditValidationError.Field.CTCP_COMMAND_PATTERN);
      }
      if (REGEX.equals(commandMode)) {
        IrcEventNotificationRuleEditValidationError error =
            regexError(
                IrcEventNotificationRuleEditValidationError.Field.CTCP_COMMAND_PATTERN,
                values.ctcpCommandPattern());
        if (error != null) return error;
      }
    }

    String valueMode = mode(values.ctcpValueMode(), ANY);
    if (ctcpPatternRequired(valueMode)) {
      if (values.ctcpValuePattern().isEmpty()) {
        return required(IrcEventNotificationRuleEditValidationError.Field.CTCP_VALUE_PATTERN);
      }
      if (REGEX.equals(valueMode)) {
        return regexError(
            IrcEventNotificationRuleEditValidationError.Field.CTCP_VALUE_PATTERN,
            values.ctcpValuePattern());
      }
    }

    return scriptPathError(values);
  }

  /** Returns whether script execution needs an accompanying script path value. */
  public static boolean scriptPathRequired(boolean scriptEnabled) {
    return scriptEnabled;
  }

  /** Returns whether the selected source mode needs an accompanying pattern/list value. */
  public static boolean sourcePatternRequired(String sourceMode) {
    String mode = mode(sourceMode, ANY);
    return NICK_LIST.equals(mode) || GLOB.equals(mode) || REGEX.equals(mode);
  }

  /** Returns whether the selected channel scope needs an accompanying pattern/list value. */
  public static boolean channelPatternsRequired(String channelScope) {
    String scope = mode(channelScope, ANY);
    return ONLY.equals(scope) || ALL_EXCEPT.equals(scope);
  }

  /** Returns whether CTCP command/value filter controls are active for the selected event type. */
  public static boolean ctcpFiltersActive(String eventType) {
    return CTCP_RECEIVED.equals(mode(eventType, ""));
  }

  /** Returns whether the selected CTCP match mode needs an accompanying pattern value. */
  public static boolean ctcpPatternRequired(String ctcpMatchMode) {
    return !ANY.equals(mode(ctcpMatchMode, ANY));
  }

  private static IrcEventNotificationRuleEditValidationError scriptPathError(
      IrcEventNotificationRuleEditValues values) {
    if (values != null
        && scriptPathRequired(values.scriptEnabled())
        && values.scriptPath().isEmpty()) {
      return required(IrcEventNotificationRuleEditValidationError.Field.SCRIPT_PATH);
    }
    return null;
  }

  private static boolean sourceNeedsPattern(String sourceMode) {
    return sourcePatternRequired(sourceMode);
  }

  private static boolean channelNeedsPattern(String channelScope) {
    return channelPatternsRequired(channelScope);
  }

  private static IrcEventNotificationRuleEditValidationError required(
      IrcEventNotificationRuleEditValidationError.Field field) {
    return new IrcEventNotificationRuleEditValidationError(
        field, IrcEventNotificationRuleEditValidationError.Reason.REQUIRED, "");
  }

  private static IrcEventNotificationRuleEditValidationError regexError(
      IrcEventNotificationRuleEditValidationError.Field field, String pattern) {
    try {
      Pattern.compile(pattern);
      return null;
    } catch (Exception ex) {
      return new IrcEventNotificationRuleEditValidationError(
          field, IrcEventNotificationRuleEditValidationError.Reason.INVALID_REGEX, ex.getMessage());
    }
  }

  private static String mode(String value, String fallback) {
    String normalized = value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    return normalized.isEmpty() ? fallback : normalized;
  }
}
