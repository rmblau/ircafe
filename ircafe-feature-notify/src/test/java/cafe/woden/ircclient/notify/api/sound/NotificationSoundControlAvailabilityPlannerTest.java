package cafe.woden.ircclient.notify.api.sound;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class NotificationSoundControlAvailabilityPlannerTest {

  @Test
  void disablesAllControlsWhenParentUnavailable() {
    NotificationSoundControlAvailabilityPlan plan =
        NotificationSoundControlAvailabilityPlanner.plan(
            false, true, true, "sounds/custom.wav", true, false);

    assertFalse(plan.enabledControlEnabled());
    assertFalse(plan.useCustomControlEnabled());
    assertFalse(plan.builtInSoundControlEnabled());
    assertFalse(plan.customPathEnabled());
    assertFalse(plan.customPathEditable());
    assertFalse(plan.browseCustomEnabled());
    assertFalse(plan.clearCustomEnabled());
    assertFalse(plan.testSoundEnabled());
  }

  @Test
  void enablesBuiltInOnlyWhenSoundIsOnAndCustomIsOff() {
    NotificationSoundControlAvailabilityPlan plan =
        NotificationSoundControlAvailabilityPlanner.plan(true, true, false, "", true, false);

    assertTrue(plan.enabledControlEnabled());
    assertTrue(plan.useCustomControlEnabled());
    assertTrue(plan.builtInSoundControlEnabled());
    assertTrue(plan.customPathEnabled());
    assertTrue(plan.customPathEditable());
    assertTrue(plan.browseCustomEnabled());
    assertFalse(plan.clearCustomEnabled());
    assertTrue(plan.testSoundEnabled());
  }

  @Test
  void canRequireCustomModeBeforeEnablingCustomFileControls() {
    NotificationSoundControlAvailabilityPlan plan =
        NotificationSoundControlAvailabilityPlanner.plan(
            true, true, false, "sounds/custom.wav", true, true);

    assertTrue(plan.builtInSoundControlEnabled());
    assertFalse(plan.customPathEnabled());
    assertFalse(plan.customPathEditable());
    assertFalse(plan.browseCustomEnabled());
    assertFalse(plan.clearCustomEnabled());

    NotificationSoundControlAvailabilityPlan customPlan =
        NotificationSoundControlAvailabilityPlanner.plan(
            true, true, true, "sounds/custom.wav", true, true);

    assertFalse(customPlan.builtInSoundControlEnabled());
    assertTrue(customPlan.customPathEnabled());
    assertTrue(customPlan.customPathEditable());
    assertTrue(customPlan.browseCustomEnabled());
    assertTrue(customPlan.clearCustomEnabled());
  }

  @Test
  void clearCustomRequiresNonBlankPath() {
    NotificationSoundControlAvailabilityPlan plan =
        NotificationSoundControlAvailabilityPlanner.plan(true, true, true, "   ", true, false);

    assertTrue(plan.customPathEnabled());
    assertFalse(plan.clearCustomEnabled());
  }
}
