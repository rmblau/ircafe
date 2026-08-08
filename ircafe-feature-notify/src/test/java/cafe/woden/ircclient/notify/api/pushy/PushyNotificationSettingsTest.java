package cafe.woden.ircclient.notify.api.pushy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PushyNotificationSettingsTest {

  @Test
  void adaptsRuntimeSettingsWithBooleanAndTimeoutNormalization() {
    PushyNotificationSettings settings =
        PushyNotificationSettings.fromRuntime(
            Boolean.TRUE,
            " https://push.example/push ",
            " secret ",
            " device ",
            " ",
            " IRCafe ",
            99,
            99);

    assertTrue(settings.enabled());
    assertEquals("https://push.example/push", settings.endpoint());
    assertEquals("secret", settings.apiKey());
    assertEquals("device", settings.deviceToken());
    assertNull(settings.topic());
    assertEquals("IRCafe", settings.titlePrefix());
    assertEquals(30, settings.connectTimeoutSeconds());
    assertEquals(60, settings.readTimeoutSeconds());
  }

  @Test
  void disabledSettingsAreUnconfiguredAndUseDefaultTimeouts() {
    PushyNotificationSettings settings = PushyNotificationSettings.disabled();

    assertFalse(settings.enabled());
    assertFalse(settings.configured());
    assertEquals(5, settings.connectTimeoutSeconds());
    assertEquals(8, settings.readTimeoutSeconds());
  }
}
