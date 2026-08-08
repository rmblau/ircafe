package cafe.woden.ircclient.bouncer;

import java.util.Objects;

/** Feature-owned preflight result for a discovered bouncer network event. */
public record BouncerDiscoveredNetworkPreflightPlan(boolean accepts, String originServerId) {

  public BouncerDiscoveredNetworkPreflightPlan {
    String origin = Objects.toString(originServerId, "").trim();
    if (accepts && origin.isEmpty()) {
      throw new IllegalArgumentException("originServerId is required");
    }
    originServerId = accepts ? origin : null;
  }

  public static BouncerDiscoveredNetworkPreflightPlan skip() {
    return new BouncerDiscoveredNetworkPreflightPlan(false, null);
  }

  public static BouncerDiscoveredNetworkPreflightPlan accept(String originServerId) {
    return new BouncerDiscoveredNetworkPreflightPlan(true, originServerId);
  }
}
