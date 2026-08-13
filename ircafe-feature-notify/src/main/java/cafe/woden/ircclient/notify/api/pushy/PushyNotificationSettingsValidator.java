package cafe.woden.ircclient.notify.api.pushy;

import java.net.URI;
import java.util.Locale;
import java.util.Objects;

/** Feature-owned validation policy for Pushy notification settings. */
public final class PushyNotificationSettingsValidator {
  private PushyNotificationSettingsValidator() {}

  public enum TargetMode {
    DEVICE_TOKEN,
    TOPIC
  }

  public enum Error {
    NONE,
    API_KEY_REQUIRED,
    DEVICE_TOKEN_REQUIRED,
    TOPIC_REQUIRED,
    ENDPOINT_INVALID
  }

  public static Error validate(
      boolean enabled, String endpoint, String apiKey, TargetMode targetMode, String targetValue) {
    if (!enabled) return Error.NONE;

    String key = trimmed(apiKey);
    if (key.isEmpty()) return Error.API_KEY_REQUIRED;

    TargetMode safeTargetMode = targetMode != null ? targetMode : TargetMode.DEVICE_TOKEN;
    String target = trimmed(targetValue);
    if (target.isEmpty()) {
      return switch (safeTargetMode) {
        case TOPIC -> Error.TOPIC_REQUIRED;
        case DEVICE_TOKEN -> Error.DEVICE_TOKEN_REQUIRED;
      };
    }

    String trimmedEndpoint = trimmed(endpoint);
    if (!trimmedEndpoint.isEmpty() && !isValidEndpoint(trimmedEndpoint)) {
      return Error.ENDPOINT_INVALID;
    }

    return Error.NONE;
  }

  public static boolean isValidEndpoint(String endpoint) {
    try {
      URI uri = URI.create(trimmed(endpoint));
      String scheme = lowerTrimmed(uri.getScheme());
      String host = trimmed(uri.getHost());
      return ("https".equals(scheme) || "http".equals(scheme)) && !host.isBlank();
    } catch (Exception ignored) {
      return false;
    }
  }

  private static String lowerTrimmed(String raw) {
    return trimmed(raw).toLowerCase(Locale.ROOT);
  }

  private static String trimmed(String raw) {
    return Objects.toString(raw, "").trim();
  }
}
