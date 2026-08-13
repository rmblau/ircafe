package cafe.woden.ircclient.notify.api.sound;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

class NotificationSoundClearSelectionPlannerTest {

  @Test
  void clearsCustomSoundSelection() {
    NotificationSoundClearSelectionPlan plan = NotificationSoundClearSelectionPlanner.plan();

    assertFalse(plan.useCustomSelected());
    assertEquals("", plan.customPath());
  }
}
