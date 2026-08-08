package cafe.woden.ircclient.notify.api.panel;

import java.util.Objects;

/** Feature-owned target normalization for notification panel rows. */
public final class NotificationPanelTargetPlanner {
  private NotificationPanelTargetPlanner() {}

  public static NotificationPanelTargetPlan plan(
      String eventServerId, String currentServerId, String channel) {
    String server = normalize(eventServerId);
    if (server.isEmpty()) server = normalize(currentServerId);
    String target = normalize(channel);
    boolean valid = !server.isEmpty() && !target.isEmpty();
    return new NotificationPanelTargetPlan(valid, valid ? server : "", valid ? target : "");
  }

  private static String normalize(String value) {
    return Objects.toString(value, "").trim();
  }
}
