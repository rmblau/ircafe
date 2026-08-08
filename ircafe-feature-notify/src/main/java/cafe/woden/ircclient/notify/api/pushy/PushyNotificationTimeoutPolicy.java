package cafe.woden.ircclient.notify.api.pushy;

/** Shared timeout normalization for Pushy runtime and preferences-derived settings. */
final class PushyNotificationTimeoutPolicy {
  static final int DEFAULT_CONNECT_TIMEOUT_SECONDS = 5;
  static final int MAX_CONNECT_TIMEOUT_SECONDS = 30;
  static final int DEFAULT_READ_TIMEOUT_SECONDS = 8;
  static final int MAX_READ_TIMEOUT_SECONDS = 60;

  private PushyNotificationTimeoutPolicy() {}

  static int normalizeConnectTimeoutSeconds(int value) {
    if (value <= 0) return DEFAULT_CONNECT_TIMEOUT_SECONDS;
    return Math.min(value, MAX_CONNECT_TIMEOUT_SECONDS);
  }

  static int normalizeReadTimeoutSeconds(int value) {
    if (value <= 0) return DEFAULT_READ_TIMEOUT_SECONDS;
    return Math.min(value, MAX_READ_TIMEOUT_SECONDS);
  }
}
