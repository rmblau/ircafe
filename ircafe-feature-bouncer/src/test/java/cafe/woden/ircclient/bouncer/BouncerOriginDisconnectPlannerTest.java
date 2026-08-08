package cafe.woden.ircclient.bouncer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.bouncer.BouncerOriginDisconnectPlan.Action;
import java.util.List;
import org.junit.jupiter.api.Test;

class BouncerOriginDisconnectPlannerTest {

  private final BouncerOriginDisconnectPlanner planner = new BouncerOriginDisconnectPlanner();

  @Test
  void blankOriginSkipsWithoutCountingEphemerals() {
    BouncerOriginDisconnectPlan plan = planner.plan(" ", List.of("origin-one"));

    assertEquals(Action.SKIP_INVALID_ORIGIN, plan.action());
    assertNull(plan.originServerId());
    assertEquals(0, plan.ephemeralCount());
    assertTrue(plan.skips());
  }

  @Test
  void noMatchingEphemeralsSkipsWithNormalizedOrigin() {
    BouncerOriginDisconnectPlan plan = planner.plan(" origin-one ", List.of("origin-two"));

    assertEquals(Action.SKIP_NO_MATCHING_EPHEMERALS, plan.action());
    assertEquals("origin-one", plan.originServerId());
    assertEquals(0, plan.ephemeralCount());
    assertFalse(plan.clearsOrigin());
  }

  @Test
  void matchingEphemeralsClearOriginWithNormalizedCount() {
    BouncerOriginDisconnectPlan plan =
        planner.plan(" origin-one ", List.of("origin-one", " origin-one ", "origin-two", " "));

    assertEquals(Action.CLEAR_ORIGIN, plan.action());
    assertEquals("origin-one", plan.originServerId());
    assertEquals(2, plan.ephemeralCount());
    assertTrue(plan.clearsOrigin());
    assertFalse(plan.skips());
  }

  @Test
  void missingEphemeralOriginsAreTreatedAsEmpty() {
    BouncerOriginDisconnectPlan plan = planner.plan("origin-one", null);

    assertEquals(Action.SKIP_NO_MATCHING_EPHEMERALS, plan.action());
    assertEquals("origin-one", plan.originServerId());
    assertEquals(0, plan.ephemeralCount());
  }

  @Test
  void clearOriginFactoryRejectsInvalidArguments() {
    assertThrows(
        IllegalArgumentException.class, () -> BouncerOriginDisconnectPlan.clearOrigin(" ", 1));
    assertThrows(
        IllegalArgumentException.class, () -> BouncerOriginDisconnectPlan.clearOrigin("origin", 0));
  }
}
