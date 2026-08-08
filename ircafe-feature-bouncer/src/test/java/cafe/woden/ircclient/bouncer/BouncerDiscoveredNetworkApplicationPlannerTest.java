package cafe.woden.ircclient.bouncer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.bouncer.BouncerDiscoveredNetworkApplicationPlan.Action;
import org.junit.jupiter.api.Test;

class BouncerDiscoveredNetworkApplicationPlannerTest {

  private final BouncerDiscoveredNetworkApplicationPlanner planner =
      new BouncerDiscoveredNetworkApplicationPlanner();

  @Test
  void persistedServerRemovesEphemeralDuplicate() {
    BouncerDiscoveredNetworkApplicationPlan plan =
        planner.plan(" server-id ", "Libera", true, true, true);

    assertEquals(Action.REMOVE_EPHEMERAL_DUPLICATE, plan.action());
    assertEquals("server-id", plan.serverId());
    assertNull(plan.autoConnectName());
    assertTrue(plan.removesEphemeralDuplicate());
    assertFalse(plan.upsertsEphemeral());
  }

  @Test
  void matchingEphemeralEntryKeepsExistingServer() {
    BouncerDiscoveredNetworkApplicationPlan plan =
        planner.plan("server-id", "Libera", false, true, true);

    assertEquals(Action.KEEP_EXISTING, plan.action());
    assertEquals("server-id", plan.serverId());
    assertNull(plan.autoConnectName());
    assertTrue(plan.keepsExisting());
    assertFalse(plan.upsertsEphemeral());
  }

  @Test
  void mismatchedEphemeralOriginIsUpserted() {
    BouncerDiscoveredNetworkApplicationPlan plan =
        planner.plan("server-id", " Libera ", false, true, false);

    assertEquals(Action.UPSERT_EPHEMERAL, plan.action());
    assertEquals("server-id", plan.serverId());
    assertEquals("Libera", plan.autoConnectName());
    assertTrue(plan.upsertsEphemeral());
  }

  @Test
  void missingEphemeralEntryIsUpserted() {
    BouncerDiscoveredNetworkApplicationPlan plan =
        planner.plan("server-id", null, false, false, false);

    assertEquals(Action.UPSERT_EPHEMERAL, plan.action());
    assertEquals("server-id", plan.serverId());
    assertNull(plan.autoConnectName());
  }

  @Test
  void blankServerIdIsRejected() {
    assertThrows(
        IllegalArgumentException.class,
        () -> planner.plan(" ", "Libera", false, false, false));
  }
}
