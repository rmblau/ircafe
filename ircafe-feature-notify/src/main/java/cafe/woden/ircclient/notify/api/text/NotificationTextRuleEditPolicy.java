package cafe.woden.ircclient.notify.api.text;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/** Feature-owned edit-time validation for notification text rules. */
public final class NotificationTextRuleEditPolicy {
  private NotificationTextRuleEditPolicy() {}

  /** Returns whether the whole-word option is meaningful for the selected text-rule type. */
  public static boolean wholeWordOptionAvailable(NotificationTextRule.Type type) {
    return normalizedType(type) == NotificationTextRule.Type.WORD;
  }

  /** Returns a whole-word flag normalized for the selected text-rule type. */
  public static boolean normalizeWholeWord(NotificationTextRule.Type type, boolean wholeWord) {
    return wholeWordOptionAvailable(type) && wholeWord;
  }

  public static List<NotificationTextRuleValidationError> validationErrors(
      List<NotificationTextRule> rules) {
    if (rules == null || rules.isEmpty()) return List.of();

    List<NotificationTextRuleValidationError> out = new ArrayList<>();
    for (int i = 0; i < rules.size(); i++) {
      NotificationTextRuleValidationError error = validateRule(i, rules.get(i));
      if (error != null) {
        out.add(error);
      }
    }
    return out;
  }

  public static NotificationTextRuleValidationError validateRule(
      int rowIndex, NotificationTextRule rule) {
    if (rule == null || !rule.enabled()) return null;
    if (rule.type() != NotificationTextRule.Type.REGEX) return null;
    if (rule.pattern().isEmpty()) return null;

    try {
      int flags = Pattern.UNICODE_CASE;
      if (!rule.caseSensitive()) flags |= Pattern.CASE_INSENSITIVE;
      Pattern.compile(rule.pattern(), flags);
      return null;
    } catch (Exception ex) {
      return new NotificationTextRuleValidationError(
          rowIndex, rule.label(), rule.pattern(), ex.getMessage());
    }
  }

  private static NotificationTextRule.Type normalizedType(NotificationTextRule.Type type) {
    return type == null ? NotificationTextRule.Type.WORD : type;
  }
}
