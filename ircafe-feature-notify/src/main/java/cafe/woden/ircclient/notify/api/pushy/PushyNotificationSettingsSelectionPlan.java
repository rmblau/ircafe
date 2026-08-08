package cafe.woden.ircclient.notify.api.pushy;

import java.util.Objects;

/** Feature-owned normalized Pushy settings selected from a preferences form. */
public record PushyNotificationSettingsSelectionPlan(
    boolean enabled,
    String endpoint,
    String apiKey,
    String deviceToken,
    String topic,
    String titlePrefix,
    int connectTimeoutSeconds,
    int readTimeoutSeconds) {

  public PushyNotificationSettingsSelectionPlan {
    endpoint = trimToNull(endpoint);
    apiKey = trimToNull(apiKey);
    deviceToken = trimToNull(deviceToken);
    topic = trimToNull(topic);
    titlePrefix = trimToNull(titlePrefix);
    connectTimeoutSeconds =
        PushyNotificationTimeoutPolicy.normalizeConnectTimeoutSeconds(connectTimeoutSeconds);
    readTimeoutSeconds =
        PushyNotificationTimeoutPolicy.normalizeReadTimeoutSeconds(readTimeoutSeconds);
  }

  private static String trimToNull(String raw) {
    String value = Objects.toString(raw, "").trim();
    return value.isEmpty() ? null : value;
  }
}
