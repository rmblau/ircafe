package cafe.woden.ircclient.notify.api.text;

import java.util.Objects;

/** Feature-owned normalization for notification text-rule validation display values. */
public final class NotificationTextRuleValidationDisplayPlanner {
  public static final int DEFAULT_INLINE_MESSAGE_MAX_CHARS = 180;

  private NotificationTextRuleValidationDisplayPlanner() {}

  public static NotificationTextRuleValidationDisplayPlan plan(
      NotificationTextRuleValidationError error, String unnamedFallback, String defaultMessage) {
    return plan(error, unnamedFallback, defaultMessage, DEFAULT_INLINE_MESSAGE_MAX_CHARS);
  }

  public static NotificationTextRuleValidationDisplayPlan plan(
      NotificationTextRuleValidationError error,
      String unnamedFallback,
      String defaultMessage,
      int inlineMessageMaxChars) {
    if (error == null) {
      return new NotificationTextRuleValidationDisplayPlan(
          1,
          trimmedOrFallback(unnamedFallback, ""),
          truncate(defaultMessage, inlineMessageMaxChars),
          trimmed(defaultMessage),
          "");
    }

    String label = trimmed(error.label());
    String pattern = trimmed(error.pattern());
    String effectiveLabel =
        !label.isEmpty() ? label : (!pattern.isEmpty() ? pattern : trimmed(unnamedFallback));
    String message = trimmedOrFallback(error.message(), defaultMessage);

    return new NotificationTextRuleValidationDisplayPlan(
        error.rowIndex() + 1,
        effectiveLabel,
        truncate(message, inlineMessageMaxChars),
        message,
        Objects.toString(error.pattern(), ""));
  }

  private static String trimmed(String raw) {
    return Objects.toString(raw, "").trim();
  }

  private static String trimmedOrFallback(String raw, String fallback) {
    String value = trimmed(raw);
    return value.isEmpty() ? trimmed(fallback) : value;
  }

  private static String truncate(String value, int maxChars) {
    String safe = trimmed(value);
    if (maxChars < 1 || safe.length() <= maxChars) return safe;
    return safe.substring(0, maxChars) + "…";
  }
}
