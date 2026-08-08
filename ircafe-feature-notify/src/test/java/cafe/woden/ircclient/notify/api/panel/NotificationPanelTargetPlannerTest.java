package cafe.woden.ircclient.notify.api.panel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class NotificationPanelTargetPlannerTest {

  @Test
  void prefersEventServerAndNormalizesChannel() {
    NotificationPanelTargetPlan plan =
        NotificationPanelTargetPlanner.plan(" libera ", "fallback", " #ircafe ");

    assertTrue(plan.valid());
    assertEquals("libera", plan.serverId());
    assertEquals("#ircafe", plan.channel());
  }

  @Test
  void fallsBackToCurrentServerWhenEventServerIsBlank() {
    NotificationPanelTargetPlan plan =
        NotificationPanelTargetPlanner.plan(" ", " oftc ", " #help ");

    assertTrue(plan.valid());
    assertEquals("oftc", plan.serverId());
    assertEquals("#help", plan.channel());
  }

  @Test
  void rejectsBlankServerOrChannel() {
    assertFalse(NotificationPanelTargetPlanner.plan(" ", " ", "#ircafe").valid());
    assertFalse(NotificationPanelTargetPlanner.plan("libera", "fallback", " ").valid());
    assertFalse(NotificationPanelTargetPlanner.plan(null, null, null).valid());
  }
}
