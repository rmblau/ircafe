package cafe.woden.ircclient.bouncer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.bouncer.spi.BouncerDiscoveredNetwork;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class BouncerDiscoveredNetworkPreflightPlannerTest {

  private final BouncerDiscoveredNetworkPreflightPlanner planner =
      new BouncerDiscoveredNetworkPreflightPlanner();

  @Test
  void skipsMissingNetwork() {
    assertFalse(planner.plan("generic", null).accepts());
  }

  @Test
  void skipsDifferentBackends() {
    assertFalse(planner.plan("generic", network("soju", "origin-1")).accepts());
  }

  @Test
  void rejectsBlankOriginServerIdsBeforePreflight() {
    assertThrows(IllegalArgumentException.class, () -> network("generic", " "));
  }

  @Test
  void acceptsMatchingBackendAndNormalizesOrigin() {
    BouncerDiscoveredNetworkPreflightPlan plan =
        planner.plan(" generic ", network("GENERIC", " origin-1 "));

    assertTrue(plan.accepts());
    assertEquals("origin-1", plan.originServerId());
  }

  @Test
  void acceptFactoryRejectsBlankOrigins() {
    assertThrows(
        IllegalArgumentException.class, () -> BouncerDiscoveredNetworkPreflightPlan.accept(" "));
  }

  private static BouncerDiscoveredNetwork network(String backendId, String originServerId) {
    return new BouncerDiscoveredNetwork(
        backendId, originServerId, "net-1", "Libera", "Libera", null, Set.of(), Map.of());
  }
}
