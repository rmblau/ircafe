package cafe.woden.ircclient.notify.api.text;

/** Feature-owned normalized seed values for editing a plain notification text rule. */
public record NotificationTextRuleEditSeedPlan(
    String label,
    NotificationTextRule.Type type,
    String pattern,
    boolean enabled,
    boolean caseSensitive,
    boolean wholeWord,
    String highlightFg) {}
