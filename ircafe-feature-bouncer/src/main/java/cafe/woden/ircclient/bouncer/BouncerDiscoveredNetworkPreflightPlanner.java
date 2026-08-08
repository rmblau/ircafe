package cafe.woden.ircclient.bouncer;

import cafe.woden.ircclient.bouncer.spi.BouncerDiscoveredNetwork;
import java.util.Objects;

/** Feature-owned preflight rules for discovered bouncer network events. */
public final class BouncerDiscoveredNetworkPreflightPlanner {

  private final BouncerDiscoveryRuntimeRules runtimeRules = new BouncerDiscoveryRuntimeRules();

  public BouncerDiscoveredNetworkPreflightPlan plan(
      String expectedBackendId, BouncerDiscoveredNetwork network) {
    if (network == null) {
      return BouncerDiscoveredNetworkPreflightPlan.skip();
    }
    if (!runtimeRules.backendMatches(expectedBackendId, network.backendId())) {
      return BouncerDiscoveredNetworkPreflightPlan.skip();
    }
    String origin = normalize(network.originServerId());
    if (origin == null) {
      return BouncerDiscoveredNetworkPreflightPlan.skip();
    }
    return BouncerDiscoveredNetworkPreflightPlan.accept(origin);
  }

  private static String normalize(String value) {
    String v = Objects.toString(value, "").trim();
    return v.isEmpty() ? null : v;
  }
}
