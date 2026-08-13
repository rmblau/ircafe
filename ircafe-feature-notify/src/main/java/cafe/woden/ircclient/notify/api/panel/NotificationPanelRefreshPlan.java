package cafe.woden.ircclient.notify.api.panel;

import java.util.Objects;

/** Feature-owned normalized server-selection plan for notification panel refreshes. */
public record NotificationPanelRefreshPlan(boolean valid, String serverId) {
  public NotificationPanelRefreshPlan {
    serverId = Objects.toString(serverId, "");
  }

  public boolean appliesTo(String changedServerId) {
    return valid && serverId.equals(Objects.toString(changedServerId, "").trim());
  }
}
