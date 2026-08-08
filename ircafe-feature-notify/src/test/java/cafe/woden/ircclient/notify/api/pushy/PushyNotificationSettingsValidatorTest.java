package cafe.woden.ircclient.notify.api.pushy;

import static cafe.woden.ircclient.notify.api.pushy.PushyNotificationSettingsValidator.Error.API_KEY_REQUIRED;
import static cafe.woden.ircclient.notify.api.pushy.PushyNotificationSettingsValidator.Error.DEVICE_TOKEN_REQUIRED;
import static cafe.woden.ircclient.notify.api.pushy.PushyNotificationSettingsValidator.Error.ENDPOINT_INVALID;
import static cafe.woden.ircclient.notify.api.pushy.PushyNotificationSettingsValidator.Error.NONE;
import static cafe.woden.ircclient.notify.api.pushy.PushyNotificationSettingsValidator.Error.TOPIC_REQUIRED;
import static cafe.woden.ircclient.notify.api.pushy.PushyNotificationSettingsValidator.TargetMode.DEVICE_TOKEN;
import static cafe.woden.ircclient.notify.api.pushy.PushyNotificationSettingsValidator.TargetMode.TOPIC;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PushyNotificationSettingsValidatorTest {

  @Test
  void skipsValidationWhenPushyIsDisabled() {
    assertEquals(NONE, PushyNotificationSettingsValidator.validate(false, "", "", TOPIC, ""));
  }

  @Test
  void requiresApiKeyAndTargetWhenEnabled() {
    assertEquals(
        API_KEY_REQUIRED,
        PushyNotificationSettingsValidator.validate(true, "", " ", DEVICE_TOKEN, "device"));
    assertEquals(
        DEVICE_TOKEN_REQUIRED,
        PushyNotificationSettingsValidator.validate(true, "", "secret", DEVICE_TOKEN, " "));
    assertEquals(
        TOPIC_REQUIRED,
        PushyNotificationSettingsValidator.validate(true, "", "secret", TOPIC, " "));
  }

  @Test
  void defaultsMissingTargetModeToDeviceToken() {
    assertEquals(
        DEVICE_TOKEN_REQUIRED,
        PushyNotificationSettingsValidator.validate(true, "", "secret", null, " "));
  }

  @Test
  void acceptsBlankEndpointBecauseRuntimeSettingsProvideDefault() {
    assertEquals(
        NONE, PushyNotificationSettingsValidator.validate(true, " ", "secret", TOPIC, "ops"));
  }

  @Test
  void validatesHttpEndpointSchemeAndHost() {
    assertTrue(PushyNotificationSettingsValidator.isValidEndpoint(" https://push.example/push "));
    assertTrue(PushyNotificationSettingsValidator.isValidEndpoint("http://push.example/push"));
    assertFalse(PushyNotificationSettingsValidator.isValidEndpoint("ftp://push.example/push"));
    assertFalse(PushyNotificationSettingsValidator.isValidEndpoint("https:///missing-host"));
    assertEquals(
        ENDPOINT_INVALID,
        PushyNotificationSettingsValidator.validate(
            true, "ftp://push.example/push", "secret", TOPIC, "ops"));
  }
}
