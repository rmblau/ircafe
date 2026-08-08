package cafe.woden.ircclient.config.runtime.notifications;

import cafe.woden.ircclient.config.properties.PushyProperties;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Pure serialization policy for persisted Pushy notification settings. */
final class RuntimeConfigPushySettingsCodec {

  private static final String DEFAULT_ENDPOINT = "https://api.pushy.me/push";
  private static final String DEFAULT_TITLE_PREFIX = "IRCafe";
  private static final Set<String> PERSISTED_KEYS =
      Set.of(
          "enabled",
          "endpoint",
          "apiKey",
          "deviceToken",
          "topic",
          "titlePrefix",
          "connectTimeoutSeconds",
          "readTimeoutSeconds");

  private RuntimeConfigPushySettingsCodec() {}

  static Map<String, Object> mergeSettings(Map<String, Object> current, PushyProperties settings) {
    Map<String, Object> out = new LinkedHashMap<>();
    if (current != null) out.putAll(current);
    PERSISTED_KEYS.forEach(out::remove);
    out.putAll(serializeSettings(settings));
    return out;
  }

  static Map<String, Object> serializeSettings(PushyProperties settings) {
    PushyProperties safe = settings == null ? disabledSettings() : settings;
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("enabled", safe.enabled());
    putOptionalString(out, "endpoint", safe.endpoint(), DEFAULT_ENDPOINT);
    putOptionalString(out, "apiKey", safe.apiKey(), null);
    putOptionalString(out, "deviceToken", safe.deviceToken(), null);
    putOptionalString(out, "topic", safe.topic(), null);
    putOptionalString(out, "titlePrefix", safe.titlePrefix(), DEFAULT_TITLE_PREFIX);
    out.put("connectTimeoutSeconds", safe.connectTimeoutSeconds());
    out.put("readTimeoutSeconds", safe.readTimeoutSeconds());
    return out;
  }

  private static PushyProperties disabledSettings() {
    return new PushyProperties(false, null, null, null, null, null, null, null);
  }

  private static void putOptionalString(
      Map<String, Object> target, String key, String value, String defaultValue) {
    String normalized = Objects.toString(value, "").trim();
    if (!normalized.isEmpty() && !normalized.equals(defaultValue)) {
      target.put(key, normalized);
    }
  }
}
