package cafe.woden.ircclient.notify.api.irc;

import java.util.List;

/** Feature-safe result of evaluating IRC-event notification match rules for one dispatch. */
public record IrcEventNotificationRuleEvaluation(
    IrcEventNotificationDispatchContext context, List<Integer> matchedRuleIndexes) {

  public IrcEventNotificationRuleEvaluation {
    context = context != null ? context : IrcEventNotificationDispatchContext.invalid();
    matchedRuleIndexes = matchedRuleIndexes != null ? List.copyOf(matchedRuleIndexes) : List.of();
  }

  public boolean valid() {
    return context.valid();
  }

  public boolean anyMatched() {
    return !matchedRuleIndexes.isEmpty();
  }

  public static IrcEventNotificationRuleEvaluation invalid() {
    return new IrcEventNotificationRuleEvaluation(
        IrcEventNotificationDispatchContext.invalid(), List.of());
  }
}
