package cafe.woden.ircclient.notify.api.irc;

import java.util.ArrayList;
import java.util.List;

/** Feature-owned rule evaluation flow for IRC-event notification dispatch. */
public final class IrcEventNotificationRuleEvaluator {
  private IrcEventNotificationRuleEvaluator() {}

  public static IrcEventNotificationRuleEvaluation evaluate(
      List<IrcEventNotificationMatchRule> rules,
      String eventType,
      String eventTypeLabel,
      String serverId,
      String channel,
      String sourceNick,
      Boolean sourceIsSelf,
      String title,
      String body,
      String activeServerId,
      String activeTarget,
      String ctcpCommand,
      String ctcpValue) {
    if (rules == null || rules.isEmpty()) return IrcEventNotificationRuleEvaluation.invalid();

    IrcEventNotificationDispatchContext context =
        IrcEventNotificationDispatchContextPlanner.plan(
            eventTypeLabel,
            serverId,
            channel,
            sourceNick,
            title,
            body,
            activeServerId,
            activeTarget);
    if (!context.valid()) {
      return new IrcEventNotificationRuleEvaluation(context, List.of());
    }

    IrcEventNotificationMatchEvent matchEvent =
        IrcEventNotificationMatchEvent.of(
            eventType,
            sourceNick,
            sourceIsSelf,
            channel,
            context.activeTargetOnSameServer(),
            context.activeTarget(),
            ctcpCommand,
            ctcpValue);

    List<Integer> matched = new ArrayList<>();
    for (int i = 0; i < rules.size(); i++) {
      IrcEventNotificationMatchRule rule = rules.get(i);
      if (rule != null && IrcEventNotificationMatchPolicy.matches(rule, matchEvent)) {
        matched.add(i);
      }
    }

    return new IrcEventNotificationRuleEvaluation(context, matched);
  }
}
