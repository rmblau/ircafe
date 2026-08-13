package cafe.woden.ircclient.notify.api.pushy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import cafe.woden.ircclient.notify.api.pushy.PushyNotificationSettingsValidator.TargetMode;
import org.junit.jupiter.api.Test;

class PushyNotificationTargetSelectionPlannerTest {

  @Test
  void initialSelectionPrefersTrimmedDeviceToken() {
    PushyNotificationTargetSelectionPlan plan =
        PushyNotificationTargetSelectionPlanner.planInitial(" device-token ", " topic-name ");

    assertEquals(TargetMode.DEVICE_TOKEN, plan.targetMode());
    assertEquals("device-token", plan.targetValue());
    assertEquals("device-token", plan.deviceToken());
    assertNull(plan.topic());
  }

  @Test
  void initialSelectionFallsBackToTrimmedTopic() {
    PushyNotificationTargetSelectionPlan plan =
        PushyNotificationTargetSelectionPlanner.planInitial(" ", " topic-name ");

    assertEquals(TargetMode.TOPIC, plan.targetMode());
    assertEquals("topic-name", plan.targetValue());
    assertNull(plan.deviceToken());
    assertEquals("topic-name", plan.topic());
  }

  @Test
  void initialSelectionDefaultsToBlankTopicWhenNoTargetExists() {
    PushyNotificationTargetSelectionPlan plan =
        PushyNotificationTargetSelectionPlanner.planInitial(null, " ");

    assertEquals(TargetMode.TOPIC, plan.targetMode());
    assertEquals("", plan.targetValue());
    assertNull(plan.deviceToken());
    assertNull(plan.topic());
  }

  @Test
  void selectedDeviceTokenClearsTopic() {
    PushyNotificationTargetSelectionPlan plan =
        PushyNotificationTargetSelectionPlanner.planSelected(TargetMode.DEVICE_TOKEN, " token ");

    assertEquals(TargetMode.DEVICE_TOKEN, plan.targetMode());
    assertEquals("token", plan.targetValue());
    assertEquals("token", plan.deviceToken());
    assertNull(plan.topic());
  }

  @Test
  void selectedTopicClearsDeviceToken() {
    PushyNotificationTargetSelectionPlan plan =
        PushyNotificationTargetSelectionPlanner.planSelected(TargetMode.TOPIC, " topic ");

    assertEquals(TargetMode.TOPIC, plan.targetMode());
    assertEquals("topic", plan.targetValue());
    assertNull(plan.deviceToken());
    assertEquals("topic", plan.topic());
  }

  @Test
  void selectedBlankTargetClearsBothDestinations() {
    PushyNotificationTargetSelectionPlan plan =
        PushyNotificationTargetSelectionPlanner.planSelected(TargetMode.DEVICE_TOKEN, " ");

    assertEquals(TargetMode.DEVICE_TOKEN, plan.targetMode());
    assertEquals("", plan.targetValue());
    assertNull(plan.deviceToken());
    assertNull(plan.topic());
  }

  @Test
  void selectedNullModeDefaultsToDeviceToken() {
    PushyNotificationTargetSelectionPlan plan =
        PushyNotificationTargetSelectionPlanner.planSelected(null, " token ");

    assertEquals(TargetMode.DEVICE_TOKEN, plan.targetMode());
    assertEquals("token", plan.deviceToken());
    assertNull(plan.topic());
  }
}
