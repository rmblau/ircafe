package cafe.woden.ircclient.bouncer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.bouncer.spi.BouncerBackendDiscoveryHandler;
import cafe.woden.ircclient.bouncer.spi.BouncerDiscoveredNetwork;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class BouncerDiscoveryHandlerCatalogTest {

  @Test
  void indexesHandlersByNormalizedBackendId() {
    RecordingHandler handler = new RecordingHandler(" Plugin-Bouncer ");

    BouncerDiscoveryHandlerCatalog catalog =
        BouncerDiscoveryHandlerCatalog.fromHandlers(List.of(handler));

    assertEquals(List.of("plugin-bouncer"), List.copyOf(catalog.backendIds()));
    assertSame(handler, catalog.handler("PLUGIN-BOUNCER").orElseThrow());
  }

  @Test
  void keepsFirstHandlerForDuplicateBackendId() {
    RecordingHandler first = new RecordingHandler("generic");
    RecordingHandler duplicate = new RecordingHandler(" GENERIC ");

    BouncerDiscoveryHandlerCatalog catalog =
        BouncerDiscoveryHandlerCatalog.fromHandlers(List.of(first, duplicate));

    assertSame(first, catalog.handler("generic").orElseThrow());
  }

  @Test
  void skipsNullBlankAndMissingHandlers() {
    RecordingHandler blank = new RecordingHandler(" ");
    BouncerDiscoveryHandlerCatalog catalog =
        BouncerDiscoveryHandlerCatalog.fromHandlers(Arrays.asList(null, blank));

    assertTrue(catalog.backendIds().isEmpty());
    assertTrue(catalog.handler("generic").isEmpty());
  }

  @Test
  void handlesNullSourceAndLookupValues() {
    BouncerDiscoveryHandlerCatalog catalog = BouncerDiscoveryHandlerCatalog.fromHandlers(null);

    assertTrue(catalog.backendIds().isEmpty());
    assertTrue(catalog.handler(null).isEmpty());
    assertTrue(catalog.handler(" ").isEmpty());
  }

  @Test
  void returnedBackendIdsCannotMutateCatalog() {
    RecordingHandler handler = new RecordingHandler("generic");
    BouncerDiscoveryHandlerCatalog catalog =
        BouncerDiscoveryHandlerCatalog.fromHandlers(List.of(handler));

    Set<String> backendIds = catalog.backendIds();

    assertThrows(UnsupportedOperationException.class, () -> backendIds.add("other"));
    assertEquals(List.of("generic"), List.copyOf(catalog.backendIds()));
  }

  private static final class RecordingHandler implements BouncerBackendDiscoveryHandler {
    private final String backendId;

    private RecordingHandler(String backendId) {
      this.backendId = backendId;
    }

    @Override
    public String backendId() {
      return backendId;
    }

    @Override
    public void onNetworkDiscovered(BouncerDiscoveredNetwork network) {
      // Not needed for catalog tests.
    }

    @Override
    public void onOriginDisconnected(String originServerId) {
      // Not needed for catalog tests.
    }
  }
}
