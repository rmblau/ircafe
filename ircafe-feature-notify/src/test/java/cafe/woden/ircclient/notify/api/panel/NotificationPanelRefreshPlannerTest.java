package cafe.woden.ircclient.notify.api.panel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class NotificationPanelRefreshPlannerTest {

  @Test
  void normalizesServerIdForRefreshes() {
    NotificationPanelRefreshPlan plan = NotificationPanelRefreshPlanner.plan(" libera ");

    assertTrue(plan.valid());
    assertEquals("libera", plan.serverId());
  }

  @Test
  void rejectsBlankServerIds() {
    assertFalse(NotificationPanelRefreshPlanner.plan(" ").valid());
    assertFalse(NotificationPanelRefreshPlanner.plan(null).valid());
  }

  @Test
  void checksWhetherChangeAppliesToCurrentServer() {
    NotificationPanelRefreshPlan plan = NotificationPanelRefreshPlanner.plan(" libera ");

    assertTrue(plan.appliesTo("libera"));
    assertTrue(plan.appliesTo(" libera "));
    assertFalse(plan.appliesTo("oftc"));
    assertFalse(NotificationPanelRefreshPlanner.shouldRefreshForChange(" ", "libera"));
  }
}
