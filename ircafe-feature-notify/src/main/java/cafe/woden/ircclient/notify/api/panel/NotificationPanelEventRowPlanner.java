package cafe.woden.ircclient.notify.api.panel;

import java.time.Instant;
import java.util.Objects;

/** Feature-owned notification panel event-row display and ordering policy. */
public final class NotificationPanelEventRowPlanner {
  private NotificationPanelEventRowPlanner() {}

  public static NotificationPanelEventRowPlan highlight(String mentionLabel, String snippet) {
    return new NotificationPanelEventRowPlan(text(mentionLabel), text(snippet));
  }

  public static NotificationPanelEventRowPlan ruleMatch(String ruleLabel, String snippet) {
    return new NotificationPanelEventRowPlan(text(ruleLabel), text(snippet));
  }

  public static NotificationPanelEventRowPlan ircEvent(String title, String body) {
    return new NotificationPanelEventRowPlan(text(title), text(body));
  }

  public static int compareNewestFirst(Instant leftAt, Instant rightAt) {
    if (leftAt == null && rightAt == null) return 0;
    if (leftAt == null) return 1;
    if (rightAt == null) return -1;
    return rightAt.compareTo(leftAt);
  }

  private static String text(Object value) {
    return Objects.toString(value, "");
  }
}
