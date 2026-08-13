package cafe.woden.ircclient.notify.api.text;

import java.util.Objects;

/** Compile-time diagnostic for a notification text rule. */
public record NotificationTextRuleCompileFailure(String ruleLabel, String message) {

  public NotificationTextRuleCompileFailure {
    ruleLabel = Objects.toString(ruleLabel, "").trim();
    message = Objects.toString(message, "").trim();
  }
}
