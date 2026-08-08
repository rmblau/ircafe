package cafe.woden.ircclient.notify.api.sound;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class NotificationSoundImportSelectionPlannerTest {

  @Test
  void appliesTrimmedImportedPathAsCustomSound() {
    NotificationSoundImportSelectionPlan plan =
        NotificationSoundImportSelectionPlanner.plan(" sounds/custom.wav ");

    assertTrue(plan.applyCustomSound());
    assertEquals("sounds/custom.wav", plan.customPath());
  }

  @Test
  void skipsBlankImportedPath() {
    NotificationSoundImportSelectionPlan plan = NotificationSoundImportSelectionPlanner.plan("  ");

    assertFalse(plan.applyCustomSound());
    assertNull(plan.customPath());
  }

  @Test
  void skipsNullImportedPath() {
    NotificationSoundImportSelectionPlan plan = NotificationSoundImportSelectionPlanner.plan(null);

    assertFalse(plan.applyCustomSound());
    assertNull(plan.customPath());
  }
}
