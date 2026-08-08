package cafe.woden.ircclient.notify.api.irc;

import java.util.Objects;

/** Feature-safe IRC event notification match input. */
public record IrcEventNotificationMatchEvent(
    String eventType,
    String sourceNick,
    Boolean sourceIsSelf,
    String channel,
    boolean activeTargetOnSameServer,
    String activeTarget,
    String ctcpCommand,
    String ctcpValue) {

  public IrcEventNotificationMatchEvent {
    eventType = trimToNull(eventType);
  }

  public static IrcEventNotificationMatchEvent of(
      String eventType,
      String sourceNick,
      Boolean sourceIsSelf,
      String channel,
      boolean activeTargetOnSameServer,
      String activeTarget,
      String ctcpCommand,
      String ctcpValue) {
    return new IrcEventNotificationMatchEvent(
        eventType,
        sourceNick,
        sourceIsSelf,
        channel,
        activeTargetOnSameServer,
        activeTarget,
        ctcpCommand,
        ctcpValue);
  }

  private static String trimToNull(String raw) {
    String value = Objects.toString(raw, "").trim();
    return value.isEmpty() ? null : value;
  }
}
