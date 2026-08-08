package cafe.woden.ircclient.bouncer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.bouncer.spi.BouncerBackendDiscoveryHandler;
import cafe.woden.ircclient.bouncer.spi.BouncerDiscoveredNetwork;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class BouncerDiscoveryEventRouterTest {

  @Test
  void routesNetworkAndDisconnectEventsByNormalizedBackendId() {
    RecordingHandler handler = new RecordingHandler(" Plugin-Bouncer ");
    BouncerDiscoveryEventRouter router =
        BouncerDiscoveryEventRouter.fromHandlers(List.of(handler));
    BouncerDiscoveredNetwork network =
        new BouncerDiscoveredNetwork(
            "PLUGIN-BOUNCER", "origin", "network", "Network", "Network", Map.of());

    assertTrue(router.routeNetworkDiscovered(network));
    assertTrue(router.routeOriginDisconnected("PLUGIN-BOUNCER", "origin"));

    assertEquals(List.of("network"), handler.discoveredNetworkIds);
    assertEquals(List.of("origin"), handler.disconnectedOriginServerIds);
    assertEquals(List.of("plugin-bouncer"), List.copyOf(router.backendIds()));
  }

  @Test
  void reportsUnroutedEventsWithoutInvokingOtherHandlers() {
    RecordingHandler handler = new RecordingHandler("generic");
    BouncerDiscoveryEventRouter router =
        BouncerDiscoveryEventRouter.fromHandlers(List.of(handler));
    BouncerDiscoveredNetwork network =
        new BouncerDiscoveredNetwork(
            "other", "origin", "network", "Network", "Network", Map.of());

    assertFalse(router.routeNetworkDiscovered(null));
    assertFalse(router.routeNetworkDiscovered(network));
    assertFalse(router.routeOriginDisconnected(null, "origin"));
    assertFalse(router.routeOriginDisconnected("other", "origin"));

    assertTrue(handler.discoveredNetworkIds.isEmpty());
    assertTrue(handler.disconnectedOriginServerIds.isEmpty());
  }

  @Test
  void keepsFirstDuplicateHandlerThroughCatalogRouting() {
    RecordingHandler first = new RecordingHandler("generic");
    RecordingHandler duplicate = new RecordingHandler(" GENERIC ");
    BouncerDiscoveryEventRouter router =
        BouncerDiscoveryEventRouter.fromHandlers(List.of(first, duplicate));
    BouncerDiscoveredNetwork network =
        new BouncerDiscoveredNetwork(
            "generic", "origin", "network", "Network", "Network", Map.of());

    assertTrue(router.routeNetworkDiscovered(network));

    assertEquals(List.of("network"), first.discoveredNetworkIds);
    assertTrue(duplicate.discoveredNetworkIds.isEmpty());
  }

  @Test
  void propagatesHandlerFailuresWithoutMaskingThem() {
    RuntimeException failure = new RuntimeException("boom");
    BouncerBackendDiscoveryHandler handler =
        new BouncerBackendDiscoveryHandler() {
          @Override
          public String backendId() {
            return "generic";
          }

          @Override
          public void onNetworkDiscovered(BouncerDiscoveredNetwork network) {
            throw failure;
          }

          @Override
          public void onOriginDisconnected(String originServerId) {
            throw failure;
          }
        };
    BouncerDiscoveryEventRouter router =
        BouncerDiscoveryEventRouter.fromHandlers(List.of(handler));
    BouncerDiscoveredNetwork network =
        new BouncerDiscoveredNetwork(
            "generic", "origin", "network", "Network", "Network", Map.of());

    assertSame(
        failure,
        assertThrows(RuntimeException.class, () -> router.routeNetworkDiscovered(network)));
    assertSame(
        failure,
        assertThrows(
            RuntimeException.class,
            () -> router.routeOriginDisconnected("generic", "origin")));
  }

  private static final class RecordingHandler implements BouncerBackendDiscoveryHandler {
    private final String backendId;
    private final List<String> discoveredNetworkIds = new ArrayList<>();
    private final List<String> disconnectedOriginServerIds = new ArrayList<>();

    private RecordingHandler(String backendId) {
      this.backendId = backendId;
    }

    @Override
    public String backendId() {
      return backendId;
    }

    @Override
    public void onNetworkDiscovered(BouncerDiscoveredNetwork network) {
      discoveredNetworkIds.add(network.networkId());
    }

    @Override
    public void onOriginDisconnected(String originServerId) {
      disconnectedOriginServerIds.add(originServerId);
    }
  }
}
