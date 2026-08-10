package cafe.woden.ircclient.notify.api.pushy;

import java.util.Objects;

/** Feature-safe Pushy send result and optional diagnostic body sample. */
public record PushyNotificationSendResult(boolean success, String message, String diagnosticBody) {

  public PushyNotificationSendResult {
    message = Objects.toString(message, "").trim();
    diagnosticBody = Objects.toString(diagnosticBody, "").trim();
  }
}
