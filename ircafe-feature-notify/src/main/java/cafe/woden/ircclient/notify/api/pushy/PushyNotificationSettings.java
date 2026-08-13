package cafe.woden.ircclient.notify.api.pushy;

import java.util.Objects;

/** Feature-safe Pushy settings values adapted from root runtime configuration. */
public record PushyNotificationSettings(
    boolean enabled,
    String endpoint,
    String apiKey,
    String deviceToken,
    String topic,
    String titlePrefix,
    int connectTimeoutSeconds,
    int readTimeoutSeconds) {

  public PushyNotificationSettings {
    endpoint = trimToNull(endpoint);
    apiKey = trimToNull(apiKey);
    deviceToken = trimToNull(deviceToken);
    topic = trimToNull(topic);
    titlePrefix = Objects.toString(titlePrefix, "").trim();
    connectTimeoutSeconds =
        PushyNotificationTimeoutPolicy.normalizeConnectTimeoutSeconds(connectTimeoutSeconds);
    readTimeoutSeconds =
        PushyNotificationTimeoutPolicy.normalizeReadTimeoutSeconds(readTimeoutSeconds);
  }

  public static PushyNotificationSettings disabled() {
    return fromRuntime(false, null, null, null, null, null, null, null);
  }

  public static PushyNotificationSettings fromRuntime(
      Boolean enabled,
      String endpoint,
      String apiKey,
      String deviceToken,
      String topic,
      String titlePrefix,
      Integer connectTimeoutSeconds,
      Integer readTimeoutSeconds) {
    return new PushyNotificationSettings(
        Boolean.TRUE.equals(enabled),
        endpoint,
        apiKey,
        deviceToken,
        topic,
        titlePrefix,
        connectTimeoutSeconds != null ? connectTimeoutSeconds : 0,
        readTimeoutSeconds != null ? readTimeoutSeconds : 0);
  }

  public boolean configured() {
    if (!enabled) return false;
    if (apiKey == null || apiKey.isBlank()) return false;
    return (deviceToken != null && !deviceToken.isBlank()) || (topic != null && !topic.isBlank());
  }

  private static String trimToNull(String raw) {
    String s = Objects.toString(raw, "").trim();
    return s.isEmpty() ? null : s;
  }
}
