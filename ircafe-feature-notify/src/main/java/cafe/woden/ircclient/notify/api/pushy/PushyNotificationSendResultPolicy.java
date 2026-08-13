package cafe.woden.ircclient.notify.api.pushy;

import java.util.Objects;

/** Feature-owned Pushy HTTP response and exception result policy. */
public final class PushyNotificationSendResultPolicy {
  static final int FAILURE_BODY_SAMPLE_LIMIT = 240;

  private PushyNotificationSendResultPolicy() {}

  public static PushyNotificationSendResult fromHttpResponse(int status, String body) {
    if (status >= 200 && status < 300) {
      return new PushyNotificationSendResult(true, "Push sent (HTTP " + status + ").", null);
    }
    return new PushyNotificationSendResult(
        false, "Pushy request failed (" + status + ").", failureBodySample(body));
  }

  public static PushyNotificationSendResult fromException(Throwable error) {
    String message = "";
    if (error != null) {
      message = Objects.toString(error.getMessage(), "").trim();
      if (message.isEmpty()) {
        message = error.getClass().getSimpleName();
      }
    }
    if (message.isEmpty()) {
      message = "Pushy request failed.";
    }
    return new PushyNotificationSendResult(false, message, null);
  }

  static String failureBodySample(String body) {
    String sample = Objects.toString(body, "").trim();
    if (sample.length() <= FAILURE_BODY_SAMPLE_LIMIT) {
      return sample;
    }
    return sample.substring(0, FAILURE_BODY_SAMPLE_LIMIT) + "...";
  }
}
