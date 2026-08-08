package cafe.woden.ircclient.notify.api.irc;

import java.util.Objects;

/** Feature-owned normalization policy for IRC event notification dispatch values. */
public final class IrcEventNotificationDispatchContextPlanner {
  private static final String DEFAULT_TARGET = "status";
  private static final String DEFAULT_SOURCE_NICK = "server";

  private IrcEventNotificationDispatchContextPlanner() {}

  public static IrcEventNotificationDispatchContext plan(
      String eventTypeLabel,
      String serverId,
      String channel,
      String sourceNick,
      String title,
      String body,
      String activeServerId,
      String activeTarget) {
    String sid = trim(serverId);
    if (sid.isEmpty()) {
      return IrcEventNotificationDispatchContext.invalid();
    }

    String target = defaultIfBlank(channel, DEFAULT_TARGET);
    String source = defaultIfBlank(sourceNick, DEFAULT_SOURCE_NICK);
    String displayTitle = defaultIfBlank(title, trim(eventTypeLabel));
    String activeSid = trim(activeServerId);
    boolean activeTargetOnSameServer = !activeSid.isEmpty() && sid.equalsIgnoreCase(activeSid);

    return new IrcEventNotificationDispatchContext(
        true,
        sid,
        target,
        source,
        displayTitle,
        trim(body),
        trim(activeTarget),
        activeTargetOnSameServer);
  }

  private static String defaultIfBlank(String raw, String defaultValue) {
    String value = trim(raw);
    return value.isEmpty() ? Objects.toString(defaultValue, "").trim() : value;
  }

  private static String trim(String raw) {
    return Objects.toString(raw, "").trim();
  }
}
