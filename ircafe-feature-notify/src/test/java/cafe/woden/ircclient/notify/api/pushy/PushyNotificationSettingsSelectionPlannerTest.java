package cafe.woden.ircclient.notify.api.pushy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.notify.api.pushy.PushyNotificationSettingsValidator.TargetMode;
import org.junit.jupiter.api.Test;

class PushyNotificationSettingsSelectionPlannerTest {

  @Test
  void trimsAndSelectsDeviceTokenSettings() {
    PushyNotificationSettingsSelectionPlan plan =
        PushyNotificationSettingsSelectionPlanner.plan(
            true,
            " https://push.example/push ",
            " secret ",
            TargetMode.DEVICE_TOKEN,
            " device-token ",
            " IRCafe Prod ",
            12,
            34);

    assertTrue(plan.enabled());
    assertEquals("https://push.example/push", plan.endpoint());
    assertEquals("secret", plan.apiKey());
    assertEquals("device-token", plan.deviceToken());
    assertNull(plan.topic());
    assertEquals("IRCafe Prod", plan.titlePrefix());
    assertEquals(12, plan.connectTimeoutSeconds());
    assertEquals(34, plan.readTimeoutSeconds());
  }

  @Test
  void trimsAndSelectsTopicSettings() {
    PushyNotificationSettingsSelectionPlan plan =
        PushyNotificationSettingsSelectionPlanner.plan(
            true,
            " https://push.example/push ",
            " secret ",
            TargetMode.TOPIC,
            " topic-name ",
            " IRCafe Prod ",
            5,
            8);

    assertNull(plan.deviceToken());
    assertEquals("topic-name", plan.topic());
  }

  @Test
  void convertsBlankOptionalValuesToNull() {
    PushyNotificationSettingsSelectionPlan plan =
        PushyNotificationSettingsSelectionPlanner.plan(
            false, " ", " ", TargetMode.TOPIC, " ", " ", 5, 8);

    assertNull(plan.endpoint());
    assertNull(plan.apiKey());
    assertNull(plan.deviceToken());
    assertNull(plan.topic());
    assertNull(plan.titlePrefix());
  }

  @Test
  void clampsSelectedTimeoutsLikeRuntimeSettings() {
    PushyNotificationSettingsSelectionPlan defaults =
        PushyNotificationSettingsSelectionPlanner.plan(
            false, null, null, TargetMode.DEVICE_TOKEN, null, null, 0, -1);
    PushyNotificationSettingsSelectionPlan capped =
        PushyNotificationSettingsSelectionPlanner.plan(
            true, null, null, TargetMode.DEVICE_TOKEN, null, null, 99, 99);

    assertEquals(5, defaults.connectTimeoutSeconds());
    assertEquals(8, defaults.readTimeoutSeconds());
    assertEquals(30, capped.connectTimeoutSeconds());
    assertEquals(60, capped.readTimeoutSeconds());
  }
}
