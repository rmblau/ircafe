package cafe.woden.ircclient.notify.api.text;

import java.util.Objects;

/** Feature-safe validation error for a notification text rule edit. */
public record NotificationTextRuleValidationError(
    int rowIndex, String label, String pattern, String message) {

  public NotificationTextRuleValidationError {
    label = Objects.toString(label, "").trim();
    pattern = Objects.toString(pattern, "").trim();
    message = Objects.toString(message, "").trim();
  }
}
