package cafe.woden.ircclient.notify.api.sound;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class NotificationSoundPreviewPlannerTest {

  @Test
  void plansBuiltInPreviewWhenCustomIsNotSelected() {
    NotificationSoundPreviewPlan plan = NotificationSoundPreviewPlanner.plan(false, "sounds/a.wav");

    assertEquals(NotificationSoundPreviewPlan.Action.BUILT_IN_SOUND, plan.action());
    assertNull(plan.customPath());
  }

  @Test
  void plansTrimmedCustomPreviewWhenCustomPathIsPresent() {
    NotificationSoundPreviewPlan plan = NotificationSoundPreviewPlanner.plan(true, " sounds/a.wav ");

    assertEquals(NotificationSoundPreviewPlan.Action.CUSTOM_FILE, plan.action());
    assertEquals("sounds/a.wav", plan.customPath());
  }

  @Test
  void skipsCustomPreviewWhenCustomPathIsBlank() {
    NotificationSoundPreviewPlan plan = NotificationSoundPreviewPlanner.plan(true, "  ");

    assertEquals(NotificationSoundPreviewPlan.Action.SKIP, plan.action());
    assertNull(plan.customPath());
  }
}
