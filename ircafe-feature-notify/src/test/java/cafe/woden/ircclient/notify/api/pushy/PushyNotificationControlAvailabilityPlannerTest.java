package cafe.woden.ircclient.notify.api.pushy;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PushyNotificationControlAvailabilityPlannerTest {

  @Test
  void disablesPushyControlsWhenPushyIsOff() {
    PushyNotificationControlAvailabilityPlan plan =
        PushyNotificationControlAvailabilityPlanner.plan(
            false, PushyNotificationSettingsValidator.Error.NONE);

    assertFalse(plan.endpointEnabled());
    assertFalse(plan.apiKeyEnabled());
    assertFalse(plan.targetModeEnabled());
    assertFalse(plan.targetValueEnabled());
    assertFalse(plan.titlePrefixEnabled());
    assertFalse(plan.connectTimeoutEnabled());
    assertFalse(plan.readTimeoutEnabled());
    assertFalse(plan.testEnabled());
  }

  @Test
  void enablesPushyControlsAndTestButtonWhenPushySettingsAreValid() {
    PushyNotificationControlAvailabilityPlan plan =
        PushyNotificationControlAvailabilityPlanner.plan(
            true, PushyNotificationSettingsValidator.Error.NONE);

    assertTrue(plan.endpointEnabled());
    assertTrue(plan.apiKeyEnabled());
    assertTrue(plan.targetModeEnabled());
    assertTrue(plan.targetValueEnabled());
    assertTrue(plan.titlePrefixEnabled());
    assertTrue(plan.connectTimeoutEnabled());
    assertTrue(plan.readTimeoutEnabled());
    assertTrue(plan.testEnabled());
  }

  @Test
  void keepsFieldsEditableButDisablesTestButtonWhenPushySettingsAreInvalid() {
    PushyNotificationControlAvailabilityPlan plan =
        PushyNotificationControlAvailabilityPlanner.plan(
            true, PushyNotificationSettingsValidator.Error.API_KEY_REQUIRED);

    assertTrue(plan.endpointEnabled());
    assertTrue(plan.apiKeyEnabled());
    assertTrue(plan.targetModeEnabled());
    assertTrue(plan.targetValueEnabled());
    assertTrue(plan.titlePrefixEnabled());
    assertTrue(plan.connectTimeoutEnabled());
    assertTrue(plan.readTimeoutEnabled());
    assertFalse(plan.testEnabled());
  }

  @Test
  void treatsNullValidationErrorAsValidForCompatibility() {
    PushyNotificationControlAvailabilityPlan plan =
        PushyNotificationControlAvailabilityPlanner.plan(true, null);

    assertTrue(plan.testEnabled());
  }
}
