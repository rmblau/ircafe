package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class Ircv3CapabilityChangePlannerTest {

  private final Ircv3CapabilityChangePlanner planner = new Ircv3CapabilityChangePlanner();

  @Test
  void ackAndDelPlanTrackedStateTransitions() {
    Ircv3CapabilityChangePlanner.Plan ack =
        planner.plan(Ircv3CapabilityLine.parse("ACK", ":~message-tags -batch"));

    assertEquals(
        List.of(
            new Ircv3CapabilityChangePlanner.Change("ACK", "message-tags", true, true),
            new Ircv3CapabilityChangePlanner.Change("ACK", "batch", false, true)),
        ack.changes());
    assertTrue(ack.refreshConnectionFeatures());

    Ircv3CapabilityChangePlanner.Plan del =
        planner.plan(Ircv3CapabilityLine.parse("DEL", "chathistory"));
    assertEquals(
        List.of(new Ircv3CapabilityChangePlanner.Change("DEL", "chathistory", false, true)),
        del.changes());
    assertTrue(del.refreshConnectionFeatures());
  }

  @Test
  void discoveryAndNakEmitAvailabilityOnlyChanges() {
    Ircv3CapabilityChangePlanner.Plan plan =
        planner.plan(Ircv3CapabilityLine.parse("NEW", "draft/read-marker"));

    assertEquals(1, plan.changes().size());
    assertFalse(plan.changes().get(0).enabled());
    assertFalse(plan.changes().get(0).updateState());
    assertFalse(plan.refreshConnectionFeatures());
  }
}
