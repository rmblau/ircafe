package cafe.woden.ircclient.notifications.api;

import java.time.Instant;

/** A single highlight/mention event. */
public record HighlightEvent(
    String serverId,
    String channel,
    String fromNick,
    String snippet,
    Instant at,
    String messageId)
    implements NotificationEvent {}
