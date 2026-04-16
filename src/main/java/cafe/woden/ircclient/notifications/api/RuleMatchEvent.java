package cafe.woden.ircclient.notifications.api;

import java.time.Instant;

/** A rule match event (WORD/REGEX) from a channel message/action. */
public record RuleMatchEvent(
    String serverId,
    String channel,
    String fromNick,
    String ruleLabel,
    String snippet,
    Instant at,
    String messageId)
    implements NotificationEvent {}
