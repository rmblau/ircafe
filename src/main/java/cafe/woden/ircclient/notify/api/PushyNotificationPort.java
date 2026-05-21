package cafe.woden.ircclient.notify.api;

import cafe.woden.ircclient.config.PushyProperties;
import cafe.woden.ircclient.model.IrcEventNotificationRule;
import java.util.Objects;
import org.jmolecules.architecture.hexagonal.PrimaryPort;
import org.jmolecules.architecture.layered.ApplicationLayer;

@PrimaryPort
@ApplicationLayer
public interface PushyNotificationPort {
  boolean notifyEvent(
      IrcEventNotificationRule.EventType eventType,
      String serverId,
      String channel,
      String sourceNick,
      Boolean sourceIsSelf,
      String title,
      String body);

  PushResult sendTestNotification(PushyProperties settings, String title, String body);

  record PushResult(boolean success, String message) {
    public static PushResult success(String message) {
      return new PushResult(true, Objects.toString(message, "").trim());
    }

    public static PushResult failed(String message) {
      return new PushResult(false, Objects.toString(message, "").trim());
    }
  }
}
