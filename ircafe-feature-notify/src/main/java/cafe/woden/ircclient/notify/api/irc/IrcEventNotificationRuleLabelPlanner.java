package cafe.woden.ircclient.notify.api.irc;

import java.util.Objects;

/** Plans IRC-event notification rule labels without root model or Swing dependencies. */
public final class IrcEventNotificationRuleLabelPlanner {
  private IrcEventNotificationRuleLabelPlanner() {}

  public static IrcEventNotificationRuleLabelPlan plan(
      String eventLabel, String sourceLabel, String eventFallback) {
    String event = normalize(eventLabel);
    String fallback = normalize(eventFallback);
    if (event.isEmpty()) event = fallback;

    String source = normalize(sourceLabel);
    String display = source.isEmpty() ? event : event + " (" + source + ")";
    return new IrcEventNotificationRuleLabelPlan(event, source, display);
  }

  private static String normalize(String value) {
    return Objects.toString(value, "").trim();
  }
}
