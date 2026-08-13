package cafe.woden.ircclient.notify.api.text;

import java.util.Objects;

/** Feature-safe normalized presentation values for a notification text rule. */
public record NotificationTextRuleSummaryPlan(
    String label,
    NotificationTextRule.Type type,
    String pattern,
    boolean caseSensitive,
    boolean wholeWord) {

  public NotificationTextRuleSummaryPlan {
    label = Objects.toString(label, "").trim();
    type = type == null ? NotificationTextRule.Type.WORD : type;
    pattern = Objects.toString(pattern, "").trim();
    wholeWord = type == NotificationTextRule.Type.WORD && wholeWord;
  }

  public boolean wordRule() {
    return type == NotificationTextRule.Type.WORD;
  }

  public boolean patternPresent() {
    return !pattern.isEmpty();
  }

  public String effectiveLabel() {
    return !label.isEmpty() ? label : pattern;
  }
}
