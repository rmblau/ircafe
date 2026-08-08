package cafe.woden.ircclient.bouncer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cafe.woden.ircclient.bouncer.spi.BouncerDiscoveredNetwork;
import cafe.woden.ircclient.bouncer.spi.BouncerNetworkMappingStrategy;
import java.util.Map;
import org.junit.jupiter.api.Test;

class BouncerNetworkDebugLabelFormatterTest {

  private final BouncerNetworkDebugLabelFormatter formatter = new BouncerNetworkDebugLabelFormatter();

  @Test
  void formatsNonBlankDebugIdsAsSuffixes() {
    assertEquals(" (net=123)", formatter.suffixFor(" net=123 "));
  }

  @Test
  void omitsBlankDebugIds() {
    assertEquals("", formatter.suffixFor(null));
    assertEquals("", formatter.suffixFor(" "));
  }

  @Test
  void readsDebugIdFromMappingStrategy() {
    assertEquals(" (network-id)", formatter.suffixFor(new DebugStrategy(" network-id "), network()));
  }

  @Test
  void rejectsMissingStrategyOrNetwork() {
    DebugStrategy strategy = new DebugStrategy("network-id");

    assertThrows(NullPointerException.class, () -> formatter.suffixFor(null, network()));
    assertThrows(NullPointerException.class, () -> formatter.suffixFor(strategy, null));
  }

  private static BouncerDiscoveredNetwork network() {
    return new BouncerDiscoveredNetwork(
        "generic", "bouncer-1", "network-1", "Network 1", "Network 1", Map.of());
  }

  private record DebugStrategy(String debugId) implements BouncerNetworkMappingStrategy {
    @Override
    public String backendId() {
      return "generic";
    }

    @Override
    public String networkDebugId(BouncerDiscoveredNetwork network) {
      return debugId;
    }
  }
}
