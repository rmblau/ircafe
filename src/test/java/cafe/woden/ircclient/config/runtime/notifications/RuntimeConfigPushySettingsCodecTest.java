package cafe.woden.ircclient.config.runtime.notifications;

import static cafe.woden.ircclient.config.runtime.notifications.RuntimeConfigPushySettingsCodec.mergeSettings;
import static cafe.woden.ircclient.config.runtime.notifications.RuntimeConfigPushySettingsCodec.serializeSettings;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import cafe.woden.ircclient.config.properties.PushyProperties;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RuntimeConfigPushySettingsCodecTest {

  @Test
  void serializeSettingsKeepsNormalizedCustomValues() {
    PushyProperties settings =
        new PushyProperties(
            true,
            " https://push.example/push ",
            " api-key ",
            " device-token ",
            " alerts ",
            " Office IRC ",
            4,
            9);

    assertEquals(
        Map.of(
            "enabled",
            true,
            "endpoint",
            "https://push.example/push",
            "apiKey",
            "api-key",
            "deviceToken",
            "device-token",
            "topic",
            "alerts",
            "titlePrefix",
            "Office IRC",
            "connectTimeoutSeconds",
            4,
            "readTimeoutSeconds",
            9),
        serializeSettings(settings));
  }

  @Test
  void serializeSettingsOmitsDefaultAndBlankOptionalValues() {
    PushyProperties settings =
        new PushyProperties(true, " https://api.pushy.me/push ", " ", null, " ", " IRCafe ", 5, 8);

    assertEquals(
        Map.of("enabled", true, "connectTimeoutSeconds", 5, "readTimeoutSeconds", 8),
        serializeSettings(settings));
  }

  @Test
  void serializeSettingsTreatsNullAsDisabledDefaults() {
    assertEquals(
        Map.of("enabled", false, "connectTimeoutSeconds", 5, "readTimeoutSeconds", 8),
        serializeSettings(null));
  }

  @Test
  void mergeSettingsReplacesKnownValuesAndPreservesUnknownKeys() {
    Map<String, Object> current = new LinkedHashMap<>();
    current.put("customExtension", "keep-me");
    current.put("enabled", true);
    current.put("endpoint", "https://old.example/push");
    current.put("apiKey", "old-key");
    current.put("deviceToken", "old-device");
    current.put("topic", "old-topic");
    current.put("titlePrefix", "Old");
    current.put("connectTimeoutSeconds", 1);
    current.put("readTimeoutSeconds", 2);

    Map<String, Object> merged = mergeSettings(current, null);

    assertEquals("keep-me", merged.get("customExtension"));
    assertEquals(false, merged.get("enabled"));
    assertEquals(5, merged.get("connectTimeoutSeconds"));
    assertEquals(8, merged.get("readTimeoutSeconds"));
    assertFalse(merged.containsKey("endpoint"));
    assertFalse(merged.containsKey("apiKey"));
    assertFalse(merged.containsKey("deviceToken"));
    assertFalse(merged.containsKey("topic"));
    assertFalse(merged.containsKey("titlePrefix"));
  }
}
