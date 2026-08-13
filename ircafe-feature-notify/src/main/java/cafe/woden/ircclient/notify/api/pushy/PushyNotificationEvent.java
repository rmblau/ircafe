package cafe.woden.ircclient.notify.api.pushy;

/** Feature-safe event values used to build Pushy notification payloads. */
public record PushyNotificationEvent(
    String eventType,
    String serverId,
    String channel,
    String sourceNick,
    Boolean sourceIsSelf,
    String title,
    String body) {}
