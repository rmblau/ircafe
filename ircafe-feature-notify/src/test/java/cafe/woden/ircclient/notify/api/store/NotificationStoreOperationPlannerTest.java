package cafe.woden.ircclient.notify.api.store;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class NotificationStoreOperationPlannerTest {

  @Test
  void serverOperationNormalizesServerIds() {
    NotificationStoreOperationPlan plan = NotificationStoreOperationPlanner.server(" libera ");

    assertTrue(plan.valid());
    assertEquals("libera", plan.serverId());
    assertEquals("", plan.channel());
  }

  @Test
  void serverOperationRejectsBlankServerIds() {
    assertFalse(NotificationStoreOperationPlanner.server(" ").valid());
    assertFalse(NotificationStoreOperationPlanner.server(null).valid());
  }

  @Test
  void recentOperationRequiresPositiveMax() {
    NotificationStoreOperationPlan plan = NotificationStoreOperationPlanner.recent(" libera ", 3);

    assertTrue(plan.valid());
    assertEquals("libera", plan.serverId());
    assertEquals(3, plan.max());

    assertFalse(NotificationStoreOperationPlanner.recent("libera", 0).valid());
    assertFalse(NotificationStoreOperationPlanner.recent("libera", -2).valid());
  }

  @Test
  void selectedOperationRequiresSelectedRows() {
    NotificationStoreOperationPlan plan = NotificationStoreOperationPlanner.selected(" libera ", 2);

    assertTrue(plan.valid());
    assertEquals("libera", plan.serverId());
    assertEquals(2, plan.selectedCount());

    assertFalse(NotificationStoreOperationPlanner.selected("libera", 0).valid());
    assertFalse(NotificationStoreOperationPlanner.selected(" ", 2).valid());
  }

  @Test
  void channelOperationRequiresRealChannelTarget() {
    NotificationStoreOperationPlan plan =
        NotificationStoreOperationPlanner.channel(" libera ", " #IRCafe ", true, false, true);

    assertTrue(plan.valid());
    assertEquals("libera", plan.serverId());
    assertEquals("#IRCafe", plan.channel());

    assertFalse(
        NotificationStoreOperationPlanner.channel("libera", "#chan", false, false, true).valid());
    assertFalse(
        NotificationStoreOperationPlanner.channel("libera", "#chan", true, true, true).valid());
    assertFalse(
        NotificationStoreOperationPlanner.channel("libera", "status", true, false, false).valid());
    assertFalse(
        NotificationStoreOperationPlanner.channel("libera", " ", true, false, true).valid());
  }
}
