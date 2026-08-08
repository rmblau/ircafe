package cafe.woden.ircclient.notify.api.irc;

/** Feature-safe result for applying a CTCP notification rule template. */
public record IrcEventNotificationCtcpTemplatePlan(
    String eventType,
    String ctcpCommandMode,
    String ctcpCommandPattern,
    String ctcpValueMode,
    String ctcpValuePattern) {}
