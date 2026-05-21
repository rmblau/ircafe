package cafe.woden.ircclient.notifications.api;

import java.time.Instant;

/** A configured IRC event notification entry (kick/invite/mode/etc). */
public record IrcEventRuleEvent(
    String serverId,
    String channel,
    String fromNick,
    String title,
    String body,
    Instant at,
    String messageId)
    implements NotificationEvent {}
