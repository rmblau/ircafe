package cafe.woden.ircclient.notify.api.text;

/** Feature-safe result for a user notification text rule match. */
public record NotificationTextMatch(
    String ruleLabel,
    NotificationTextRule.Type ruleType,
    String matchedText,
    int start,
    int end,
    String highlightColor) {}
