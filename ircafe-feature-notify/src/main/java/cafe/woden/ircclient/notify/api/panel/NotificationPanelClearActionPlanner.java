package cafe.woden.ircclient.notify.api.panel;

/** Feature-owned preflight for notification panel clear actions. */
public final class NotificationPanelClearActionPlanner {
  private NotificationPanelClearActionPlanner() {}

  public static NotificationPanelClearActionPlan clearSelected(
      String serverId, int selectedEventCount) {
    NotificationPanelRefreshPlan server = NotificationPanelRefreshPlanner.plan(serverId);
    boolean clear = server.valid() && selectedEventCount > 0;
    return new NotificationPanelClearActionPlan(clear, server.serverId());
  }

  public static NotificationPanelClearActionPlan clearAll(String serverId, int rowCount) {
    NotificationPanelRefreshPlan server = NotificationPanelRefreshPlanner.plan(serverId);
    boolean clear = server.valid() && rowCount > 0;
    return new NotificationPanelClearActionPlan(clear, server.serverId());
  }
}
