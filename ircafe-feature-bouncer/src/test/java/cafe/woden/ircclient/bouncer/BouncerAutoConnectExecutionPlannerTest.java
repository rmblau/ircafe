package cafe.woden.ircclient.bouncer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.bouncer.BouncerAutoConnectExecutionPlan.Action;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

class BouncerAutoConnectExecutionPlannerTest {

  private final BouncerAutoConnectQueueGate queueGate = new BouncerAutoConnectQueueGate();
  private final BouncerAutoConnectExecutionPlanner planner =
      new BouncerAutoConnectExecutionPlanner(queueGate);

  @Test
  void blankServerIdSkipsWithoutReadingEnabledPreference() {
    AtomicBoolean enabledRead = new AtomicBoolean(false);

    BouncerAutoConnectExecutionPlan plan =
        planner.plan(
            " bouncer-id ",
            " Libera ",
            " ",
            () -> {
              enabledRead.set(true);
              return true;
            });

    assertEquals(Action.SKIP_INVALID_SERVER_ID, plan.action());
    assertEquals("bouncer-id", plan.bouncerId());
    assertEquals("Libera", plan.networkName());
    assertNull(plan.serverId());
    assertTrue(plan.skips());
    assertFalse(enabledRead.get());
  }

  @Test
  void disabledPreferenceSkipsWithoutQueueingServer() {
    BouncerAutoConnectExecutionPlan plan =
        planner.plan("bouncer-id", "Libera", " bouncer:bouncer-id:libera ", () -> false);

    assertEquals(Action.SKIP_DISABLED, plan.action());
    assertEquals("bouncer:bouncer-id:libera", plan.serverId());
    assertTrue(plan.skips());
    assertFalse(queueGate.isQueued("bouncer:bouncer-id:libera"));
  }

  @Test
  void enabledPreferenceQueuesAndConnectsServer() {
    BouncerAutoConnectExecutionPlan plan =
        planner.plan("bouncer-id", "Libera", " bouncer:bouncer-id:libera ", () -> true);

    assertEquals(Action.CONNECT, plan.action());
    assertEquals("bouncer-id", plan.bouncerId());
    assertEquals("Libera", plan.networkName());
    assertEquals("bouncer:bouncer-id:libera", plan.serverId());
    assertTrue(plan.connects());
    assertTrue(queueGate.isQueued("bouncer:bouncer-id:libera"));
  }

  @Test
  void queuedServerSkipsDuplicateConnectAttempt() {
    assertTrue(planner.plan("bouncer-id", "Libera", "server-id", () -> true).connects());

    BouncerAutoConnectExecutionPlan plan =
        planner.plan("bouncer-id", "Libera", "server-id", () -> true);

    assertEquals(Action.SKIP_ALREADY_QUEUED, plan.action());
    assertEquals("server-id", plan.serverId());
    assertTrue(plan.skips());
  }

  @Test
  void nullEnabledSupplierIsRejected() {
    assertThrows(
        NullPointerException.class, () -> planner.plan("bouncer-id", "Libera", "server-id", null));
  }
}
