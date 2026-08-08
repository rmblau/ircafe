package cafe.woden.ircclient.notify.api.store;

/** Feature-owned normalized preflight values for notification-store operations. */
public record NotificationStoreOperationPlan(
    boolean valid, String serverId, String channel, int max, int selectedCount) {

  public NotificationStoreOperationPlan {
    serverId = NotificationStoreEventPolicy.normalizeServerId(serverId);
    channel = NotificationStoreEventPolicy.normalizeChannel(channel);
    max = Math.max(0, max);
    selectedCount = Math.max(0, selectedCount);
  }

  static NotificationStoreOperationPlan invalid() {
    return new NotificationStoreOperationPlan(false, "", "", 0, 0);
  }
}
