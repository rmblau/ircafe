package cafe.woden.ircclient.notify.api.text;

/** Feature-owned display-safe values for a notification text-rule validation error. */
public record NotificationTextRuleValidationDisplayPlan(
    int rowNumber,
    String effectiveLabel,
    String inlineMessage,
    String dialogMessage,
    String patternForDialog) {}
