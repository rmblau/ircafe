package cafe.woden.ircclient.bouncer;

import cafe.woden.ircclient.bouncer.spi.BouncerDiscoveredNetwork;
import cafe.woden.ircclient.bouncer.spi.BouncerNetworkMappingStrategy;
import cafe.woden.ircclient.bouncer.spi.BouncerServerProfile;
import cafe.woden.ircclient.bouncer.spi.ResolvedBouncerNetwork;
import java.util.Objects;

/** Feature-owned selection and missing-provider policy for bouncer mapping strategies. */
public final class BouncerMappingStrategySelector {

  /**
   * Returns the resolved strategy, or a lazy failure strategy when the requested backend is absent.
   *
   * <p>The lazy fallback preserves application startup and reports the missing backend only if a
   * discovery event actually attempts to resolve a network.
   */
  public BouncerNetworkMappingStrategy select(
      String requestedBackendId, BouncerNetworkMappingStrategy resolvedStrategy) {
    if (resolvedStrategy != null) {
      return resolvedStrategy;
    }
    return new MissingMappingStrategy(normalizeRequestedBackendId(requestedBackendId));
  }

  private static String normalizeRequestedBackendId(String backendId) {
    return Objects.toString(backendId, "").trim();
  }

  private record MissingMappingStrategy(String backendId) implements BouncerNetworkMappingStrategy {

    @Override
    public ResolvedBouncerNetwork resolveNetwork(
        BouncerServerProfile bouncer, BouncerDiscoveredNetwork network) {
      throw new IllegalStateException("Missing bouncer mapping strategy: " + backendId);
    }
  }
}
