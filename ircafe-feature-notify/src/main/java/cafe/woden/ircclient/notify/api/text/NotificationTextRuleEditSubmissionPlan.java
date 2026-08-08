package cafe.woden.ircclient.notify.api.text;

import java.util.Objects;

/** Feature-owned normalized values produced by the plain notification rule editor. */
public record NotificationTextRuleEditSubmissionPlan(
    String label,
    NotificationTextRule.Type type,
    String pattern,
    boolean enabled,
    boolean caseSensitive,
    boolean wholeWord,
    String highlightFg) {
  public NotificationTextRuleEditSubmissionPlan {
    label = Objects.toString(label, "").trim();
    type = type == null ? NotificationTextRule.Type.WORD : type;
    pattern = Objects.toString(pattern, "").trim();
    wholeWord = NotificationTextRuleEditPolicy.normalizeWholeWord(type, wholeWord);
  }
}
