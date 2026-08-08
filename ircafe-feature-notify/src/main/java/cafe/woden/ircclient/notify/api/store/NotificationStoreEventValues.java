package cafe.woden.ircclient.notify.api.store;

/** Feature-safe normalized values for one notification-store event. */
public record NotificationStoreEventValues(
    boolean valid,
    String serverId,
    String channel,
    String fromNick,
    String label,
    String snippet,
    String messageId) {

  public NotificationStoreEventValues {
    serverId = serverId == null ? "" : serverId;
    channel = channel == null ? "" : channel;
    fromNick = fromNick == null ? "" : fromNick;
    label = label == null ? "" : label;
    snippet = snippet == null ? "" : snippet;
    messageId = messageId == null ? "" : messageId;
  }

  public static NotificationStoreEventValues invalid() {
    return new NotificationStoreEventValues(false, "", "", "", "", "", "");
  }
}
