package cafe.woden.ircclient.notify.api.irc;

import java.util.Objects;

/** Feature-safe validation error for one IRC-event notification rule edit form. */
public record IrcEventNotificationRuleEditValidationError(
    Field field, Reason reason, String message) {

  public enum Field {
    SOURCE_PATTERN,
    CHANNEL_PATTERNS,
    CTCP_COMMAND_PATTERN,
    CTCP_VALUE_PATTERN,
    SCRIPT_PATH
  }

  public enum Reason {
    REQUIRED,
    INVALID_REGEX
  }

  public IrcEventNotificationRuleEditValidationError {
    message = Objects.toString(message, "").trim();
  }
}
