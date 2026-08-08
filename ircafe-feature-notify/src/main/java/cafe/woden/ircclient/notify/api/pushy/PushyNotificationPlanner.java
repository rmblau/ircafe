package cafe.woden.ircclient.notify.api.pushy;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Objects;

/** Feature-owned Pushy request planning and payload construction policy. */
public final class PushyNotificationPlanner {
  public static final String DISABLED_MESSAGE = "Pushy is disabled.";
  public static final String MISSING_CREDENTIALS_MESSAGE =
      "Pushy endpoint and API key are required.";
  public static final String MISSING_DESTINATION_MESSAGE =
      "Set either a device token or topic destination.";
  public static final String DEFAULT_TEST_BODY = "Pushy integration test from IRCafe.";

  private PushyNotificationPlanner() {}

  public static PushyNotificationPlan planEvent(
      PushyNotificationSettings settings, PushyNotificationEvent event, long timestampMs) {
    PushyNotificationSettings safeSettings = settings != null ? settings : disabledSettings();
    if (!safeSettings.configured()) return PushyNotificationPlan.skip();

    String endpoint = Objects.toString(safeSettings.endpoint(), "").trim();
    String apiKey = Objects.toString(safeSettings.apiKey(), "").trim();
    if (endpoint.isEmpty() || apiKey.isEmpty()) return PushyNotificationPlan.skip();

    PushyNotificationEvent safeEvent = event != null ? event : emptyEvent();
    String title = buildTitle(safeSettings, safeEvent.title());
    String body = Objects.toString(safeEvent.body(), "").trim();
    if (body.isEmpty()) body = defaultEventBody(safeEvent.eventType());

    String payload = buildPayload(safeSettings, safeEvent, title, body, timestampMs);
    if (payload == null || payload.isBlank()) return PushyNotificationPlan.skip();

    return PushyNotificationPlan.send(buildUrl(endpoint, apiKey), payload);
  }

  public static PushyNotificationPlan planTest(
      PushyNotificationSettings settings, String title, String body, long timestampMs) {
    PushyNotificationSettings safeSettings = settings != null ? settings : disabledSettings();
    if (!safeSettings.enabled()) {
      return PushyNotificationPlan.failed(DISABLED_MESSAGE);
    }

    String endpoint = Objects.toString(safeSettings.endpoint(), "").trim();
    String apiKey = Objects.toString(safeSettings.apiKey(), "").trim();
    if (endpoint.isEmpty() || apiKey.isEmpty()) {
      return PushyNotificationPlan.failed(MISSING_CREDENTIALS_MESSAGE);
    }

    String finalTitle = buildTitle(safeSettings, title);
    String finalBody = Objects.toString(body, "").trim();
    if (finalBody.isEmpty()) finalBody = DEFAULT_TEST_BODY;

    PushyNotificationEvent event =
        new PushyNotificationEvent(
            "PRIVATE_MESSAGE_RECEIVED", "local", "status", "ircafe", false, finalTitle, finalBody);
    String payload = buildPayload(safeSettings, event, finalTitle, finalBody, timestampMs);
    if (payload == null || payload.isBlank()) {
      return PushyNotificationPlan.failed(MISSING_DESTINATION_MESSAGE);
    }

    return PushyNotificationPlan.send(buildUrl(endpoint, apiKey), payload);
  }

  private static String buildPayload(
      PushyNotificationSettings settings,
      PushyNotificationEvent event,
      String title,
      String body,
      long timestampMs) {
    String to = Objects.toString(settings.deviceToken(), "").trim();
    String topic = Objects.toString(settings.topic(), "").trim();
    if (to.isEmpty() && topic.isEmpty()) return null;

    StringBuilder json = new StringBuilder(512);
    json.append('{');
    if (!to.isEmpty()) {
      appendJsonField(json, "to", to);
    } else {
      appendJsonField(json, "topic", topic);
    }
    json.append(',');
    json.append("\"notification\":{");
    appendJsonField(json, "title", title);
    json.append(',');
    appendJsonField(json, "body", body);
    json.append("},");
    json.append("\"data\":{");
    appendJsonField(json, "eventType", Objects.toString(event.eventType(), ""));
    json.append(',');
    appendJsonField(json, "serverId", Objects.toString(event.serverId(), ""));
    json.append(',');
    appendJsonField(json, "channel", Objects.toString(event.channel(), ""));
    json.append(',');
    appendJsonField(json, "sourceNick", Objects.toString(event.sourceNick(), ""));
    json.append(',');
    appendJsonField(
        json,
        "sourceIsSelf",
        event.sourceIsSelf() == null ? "unknown" : event.sourceIsSelf().toString());
    json.append(',');
    appendJsonField(json, "timestampMs", Long.toString(timestampMs));
    json.append('}');
    json.append('}');
    return json.toString();
  }

  private static String buildTitle(PushyNotificationSettings settings, String title) {
    String prefix = Objects.toString(settings.titlePrefix(), "").trim();
    String value = Objects.toString(title, "").trim();
    if (value.isEmpty()) value = "IRC Event";
    if (prefix.isEmpty()) return value;
    if (value.toLowerCase(Locale.ROOT).startsWith(prefix.toLowerCase(Locale.ROOT))) return value;
    return prefix + " - " + value;
  }

  private static String defaultEventBody(String eventType) {
    String value = Objects.toString(eventType, "").trim();
    return value.isEmpty() ? "Event" : value;
  }

  private static String buildUrl(String endpoint, String apiKey) {
    return endpoint + "?api_key=" + URLEncoder.encode(apiKey, StandardCharsets.UTF_8);
  }

  private static PushyNotificationSettings disabledSettings() {
    return new PushyNotificationSettings(false, null, null, null, null, null, 5, 8);
  }

  private static PushyNotificationEvent emptyEvent() {
    return new PushyNotificationEvent(null, null, null, null, null, null, null);
  }

  private static void appendJsonField(StringBuilder out, String key, String value) {
    out.append('"').append(escapeJson(key)).append('"').append(':');
    out.append('"').append(escapeJson(Objects.toString(value, ""))).append('"');
  }

  private static String escapeJson(String raw) {
    String s = Objects.toString(raw, "");
    StringBuilder out = new StringBuilder(s.length() + 16);
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      switch (c) {
        case '"' -> out.append("\\\"");
        case '\\' -> out.append("\\\\");
        case '\b' -> out.append("\\b");
        case '\f' -> out.append("\\f");
        case '\n' -> out.append("\\n");
        case '\r' -> out.append("\\r");
        case '\t' -> out.append("\\t");
        default -> {
          if (c < 0x20) {
            out.append(String.format("\\u%04x", (int) c));
          } else {
            out.append(c);
          }
        }
      }
    }
    return out.toString();
  }
}
