package cafe.woden.ircclient.notify.api.text;

/** Feature-owned normalized shape for persisted plain text notification rules. */
public record NotificationTextPersistedRulePlan(
    boolean enabled,
    String label,
    String type,
    String pattern,
    boolean caseSensitive,
    boolean wholeWord,
    String highlightFg) {}
