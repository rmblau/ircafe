package cafe.woden.ircclient.notify.api.pushy;

import java.util.Objects;

/** Feature-owned Pushy request plan. */
public record PushyNotificationPlan(String url, String payload, String failureMessage) {
  public PushyNotificationPlan {
    url = Objects.toString(url, "").trim();
    payload = Objects.toString(payload, "").trim();
    failureMessage = Objects.toString(failureMessage, "").trim();
  }

  public boolean sendable() {
    return !url.isBlank() && !payload.isBlank() && failureMessage.isBlank();
  }

  public static PushyNotificationPlan send(String url, String payload) {
    return new PushyNotificationPlan(url, payload, null);
  }

  public static PushyNotificationPlan skip() {
    return new PushyNotificationPlan(null, null, null);
  }

  public static PushyNotificationPlan failed(String message) {
    return new PushyNotificationPlan(null, null, message);
  }
}
