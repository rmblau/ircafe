package cafe.woden.ircclient.notify.api.irc;

import java.util.Objects;

/** Feature-safe dispatch values for a matched IRC event notification. */
public record IrcEventNotificationDispatchContext(
    boolean valid,
    String serverId,
    String target,
    String sourceNick,
    String title,
    String body,
    String activeTarget,
    boolean activeTargetOnSameServer) {

  public IrcEventNotificationDispatchContext {
    serverId = Objects.toString(serverId, "").trim();
    target = Objects.toString(target, "").trim();
    sourceNick = Objects.toString(sourceNick, "").trim();
    title = Objects.toString(title, "").trim();
    body = Objects.toString(body, "").trim();
    activeTarget = Objects.toString(activeTarget, "").trim();
    if (serverId.isEmpty()) {
      valid = false;
    }
  }

  public static IrcEventNotificationDispatchContext invalid() {
    return new IrcEventNotificationDispatchContext(false, null, null, null, null, null, null, false);
  }
}
