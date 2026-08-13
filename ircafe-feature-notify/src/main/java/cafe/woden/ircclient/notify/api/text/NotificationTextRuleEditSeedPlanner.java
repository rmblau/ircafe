package cafe.woden.ircclient.notify.api.text;

import java.util.Objects;

/** Normalizes plain text-rule edit seeds without depending on root config or Swing. */
public final class NotificationTextRuleEditSeedPlanner {
  private NotificationTextRuleEditSeedPlanner() {}

  /** Default seed for adding a new rule in the edit dialog. */
  public static NotificationTextRuleEditSeedPlan defaultSeed() {
    return plan("", NotificationTextRule.Type.WORD, "", true, false, true, null);
  }

  /** Default seed for defensive table rows created from missing/null root values. */
  public static NotificationTextRuleEditSeedPlan emptyRowSeed() {
    return plan("", NotificationTextRule.Type.WORD, "", false, false, true, null);
  }

  public static NotificationTextRuleEditSeedPlan plan(
      String label,
      NotificationTextRule.Type type,
      String pattern,
      boolean enabled,
      boolean caseSensitive,
      boolean wholeWord,
      String highlightFg) {
    NotificationTextRule.Type normalizedType = type == null ? NotificationTextRule.Type.WORD : type;
    return new NotificationTextRuleEditSeedPlan(
        normalize(label),
        normalizedType,
        normalize(pattern),
        enabled,
        caseSensitive,
        NotificationTextRuleEditPolicy.normalizeWholeWord(normalizedType, wholeWord),
        normalizeNullable(highlightFg));
  }

  private static String normalize(String value) {
    return Objects.toString(value, "").trim();
  }

  private static String normalizeNullable(String value) {
    String normalized = normalize(value);
    return normalized.isEmpty() ? null : normalized;
  }
}
