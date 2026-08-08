package cafe.woden.ircclient.notify.api.text;

import java.util.Objects;

/** Feature-safe user notification text rule. */
public record NotificationTextRule(
    String label,
    Type type,
    String pattern,
    boolean enabled,
    boolean caseSensitive,
    boolean wholeWord,
    String highlightColor) {

  public enum Type {
    WORD,
    REGEX
  }

  public NotificationTextRule {
    label = Objects.toString(label, "").trim();
    type = type == null ? Type.WORD : type;
    pattern = Objects.toString(pattern, "").trim();
    wholeWord = NotificationTextRuleEditPolicy.normalizeWholeWord(type, wholeWord);
    highlightColor = Objects.toString(highlightColor, "").trim();
    if (highlightColor.isEmpty()) highlightColor = null;
    if (pattern.isEmpty()) enabled = false;
  }
}
