package cafe.woden.ircclient.notify.api.panel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class NotificationPanelClearActionPlannerTest {

  @Test
  void selectedClearRequiresServerAndSelectedEvents() {
    assertFalse(NotificationPanelClearActionPlanner.clearSelected(" ", 1).clear());
    assertFalse(NotificationPanelClearActionPlanner.clearSelected("server-a", 0).clear());

    NotificationPanelClearActionPlan plan =
        NotificationPanelClearActionPlanner.clearSelected(" server-a ", 2);

    assertTrue(plan.clear());
    assertEquals("server-a", plan.serverId());
  }

  @Test
  void clearAllRequiresServerAndRows() {
    assertFalse(NotificationPanelClearActionPlanner.clearAll(null, 1).clear());
    assertFalse(NotificationPanelClearActionPlanner.clearAll("server-a", 0).clear());

    NotificationPanelClearActionPlan plan =
        NotificationPanelClearActionPlanner.clearAll(" server-a ", 3);

    assertTrue(plan.clear());
    assertEquals("server-a", plan.serverId());
  }
}
