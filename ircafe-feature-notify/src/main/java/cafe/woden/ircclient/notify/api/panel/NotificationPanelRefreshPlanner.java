package cafe.woden.ircclient.notify.api.panel;

import java.util.Objects;

/** Feature-owned server-id normalization for notification panel refresh decisions. */
public final class NotificationPanelRefreshPlanner {
  private NotificationPanelRefreshPlanner() {}

  public static NotificationPanelRefreshPlan plan(String serverId) {
    String sid = Objects.toString(serverId, "").trim();
    return new NotificationPanelRefreshPlan(!sid.isEmpty(), sid);
  }

  public static boolean shouldRefreshForChange(String currentServerId, String changedServerId) {
    return plan(currentServerId).appliesTo(changedServerId);
  }
}
