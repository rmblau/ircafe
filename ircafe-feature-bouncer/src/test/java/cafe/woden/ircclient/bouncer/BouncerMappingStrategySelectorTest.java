package cafe.woden.ircclient.bouncer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cafe.woden.ircclient.bouncer.spi.BouncerDiscoveredNetwork;
import cafe.woden.ircclient.bouncer.spi.BouncerNetworkMappingStrategy;
import cafe.woden.ircclient.bouncer.spi.BouncerServerProfile;
import cafe.woden.ircclient.bouncer.spi.ResolvedBouncerNetwork;
import org.junit.jupiter.api.Test;

class BouncerMappingStrategySelectorTest {

  private final BouncerMappingStrategySelector selector = new BouncerMappingStrategySelector();

  @Test
  void returnsResolvedStrategyWithoutWrappingIt() {
    BouncerNetworkMappingStrategy strategy = new FakeStrategy("soju");

    assertSame(strategy, selector.select("soju", strategy));
  }

  @Test
  void createsLazyFailureStrategyForMissingBackend() {
    BouncerNetworkMappingStrategy strategy = selector.select(" soju ", null);

    assertEquals("soju", strategy.backendId());
    IllegalStateException failure =
        assertThrows(
            IllegalStateException.class,
            () -> strategy.resolveNetwork(null, null));
    assertEquals("Missing bouncer mapping strategy: soju", failure.getMessage());
  }

  @Test
  void keepsBlankMissingBackendDiagnosticDeterministic() {
    BouncerNetworkMappingStrategy strategy = selector.select(null, null);

    assertEquals("", strategy.backendId());
    IllegalStateException failure =
        assertThrows(
            IllegalStateException.class,
            () -> strategy.resolveNetwork(null, null));
    assertEquals("Missing bouncer mapping strategy: ", failure.getMessage());
  }

  private record FakeStrategy(String backendId) implements BouncerNetworkMappingStrategy {
    @Override
    public ResolvedBouncerNetwork resolveNetwork(
        BouncerServerProfile bouncer, BouncerDiscoveredNetwork network) {
      throw new UnsupportedOperationException();
    }
  }
}
