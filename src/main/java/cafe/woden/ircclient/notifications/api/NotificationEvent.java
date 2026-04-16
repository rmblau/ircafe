package cafe.woden.ircclient.notifications.api;

import java.time.Instant;

/** Public notification event contract exported by the notifications module. */
public sealed interface NotificationEvent
    permits HighlightEvent, RuleMatchEvent, IrcEventRuleEvent {

  String serverId();

  String channel();

  String fromNick();

  Instant at();

  String messageId();
}
