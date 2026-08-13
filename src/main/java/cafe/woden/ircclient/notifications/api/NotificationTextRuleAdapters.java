package cafe.woden.ircclient.notifications.api;

import cafe.woden.ircclient.config.api.NotificationRule;
import cafe.woden.ircclient.notify.api.text.NotificationTextRule;
import java.util.List;
import java.util.Objects;

/** Adapts root notification rule settings into feature-safe text-rule values. */
public final class NotificationTextRuleAdapters {
  private NotificationTextRuleAdapters() {}

  public static List<NotificationTextRule> toFeatureRules(List<NotificationRule> rules) {
    return Objects.requireNonNullElse(rules, List.<NotificationRule>of()).stream()
        .filter(Objects::nonNull)
        .map(NotificationTextRuleAdapters::toFeatureRule)
        .toList();
  }

  public static NotificationTextRule toFeatureRule(NotificationRule rule) {
    if (rule == null) return null;
    return toFeatureRule(
        rule.label(),
        rule.type(),
        rule.pattern(),
        rule.enabled(),
        rule.caseSensitive(),
        rule.wholeWord(),
        rule.highlightFg());
  }

  public static NotificationTextRule toFeatureRule(
      String label,
      NotificationRule.Type type,
      String pattern,
      boolean enabled,
      boolean caseSensitive,
      boolean wholeWord,
      String highlightColor) {
    return new NotificationTextRule(
        label, toFeatureType(type), pattern, enabled, caseSensitive, wholeWord, highlightColor);
  }

  public static NotificationTextRule.Type toFeatureType(NotificationRule.Type type) {
    return type == NotificationRule.Type.REGEX
        ? NotificationTextRule.Type.REGEX
        : NotificationTextRule.Type.WORD;
  }
}
