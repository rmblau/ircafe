package cafe.woden.ircclient.notify.api.store;

/** Plans notification-store operation preflight without root storage or UI dependencies. */
public final class NotificationStoreOperationPlanner {
  private NotificationStoreOperationPlanner() {}

  /** Normalizes a server-scoped read/count/clear operation. */
  public static NotificationStoreOperationPlan server(String serverId) {
    String sid = NotificationStoreEventPolicy.normalizeServerId(serverId);
    if (sid.isEmpty()) return NotificationStoreOperationPlan.invalid();
    return new NotificationStoreOperationPlan(true, sid, "", 0, 0);
  }

  /** Normalizes a recent-events read operation. */
  public static NotificationStoreOperationPlan recent(String serverId, int max) {
    String sid = NotificationStoreEventPolicy.normalizeServerId(serverId);
    if (sid.isEmpty() || max <= 0) return NotificationStoreOperationPlan.invalid();
    return new NotificationStoreOperationPlan(true, sid, "", max, 0);
  }

  /** Normalizes a selected-events clear operation. */
  public static NotificationStoreOperationPlan selected(String serverId, int selectedCount) {
    String sid = NotificationStoreEventPolicy.normalizeServerId(serverId);
    if (sid.isEmpty() || selectedCount <= 0) return NotificationStoreOperationPlan.invalid();
    return new NotificationStoreOperationPlan(true, sid, "", 0, selectedCount);
  }

  /** Normalizes a channel-scoped clear operation from root-owned target flags. */
  public static NotificationStoreOperationPlan channel(
      String serverId,
      String channel,
      boolean targetPresent,
      boolean uiOnly,
      boolean channelTarget) {
    if (!targetPresent || uiOnly || !channelTarget) return NotificationStoreOperationPlan.invalid();
    String sid = NotificationStoreEventPolicy.normalizeServerId(serverId);
    String normalizedChannel = NotificationStoreEventPolicy.normalizeChannel(channel);
    if (sid.isEmpty() || normalizedChannel.isEmpty()) {
      return NotificationStoreOperationPlan.invalid();
    }
    return new NotificationStoreOperationPlan(true, sid, normalizedChannel, 0, 0);
  }
}
