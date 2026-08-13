package cafe.woden.ircclient.notify.api.irc;

import java.util.Objects;

/** Feature-safe preflight decision for IRC-event notification rule dispatch. */
public record IrcEventNotificationDispatchPreflightPlan(
    boolean shouldEvaluate, String eventTypeName, int ruleCount) {
  public IrcEventNotificationDispatchPreflightPlan {
    eventTypeName = Objects.toString(eventTypeName, "").trim();
    ruleCount = Math.max(0, ruleCount);
    shouldEvaluate = shouldEvaluate && !eventTypeName.isEmpty() && ruleCount > 0;
  }

  public boolean matchedRuleIndexValid(Integer matchedIndex) {
    return matchedIndex != null && matchedIndex >= 0 && matchedIndex < ruleCount;
  }
}
