package cafe.woden.ircclient.bouncer;

import cafe.woden.ircclient.bouncer.spi.BouncerEphemeralServerSpec;
import cafe.woden.ircclient.bouncer.spi.ResolvedBouncerNetwork;
import java.util.Objects;

/** Feature-owned result of resolving a discovered bouncer network through a mapping strategy. */
public record BouncerDiscoveredNetworkResolution(
    ResolvedBouncerNetwork resolvedNetwork, BouncerEphemeralServerSpec serverSpec) {

  public BouncerDiscoveredNetworkResolution {
    resolvedNetwork = Objects.requireNonNull(resolvedNetwork, "resolvedNetwork");
    serverSpec = Objects.requireNonNull(serverSpec, "serverSpec");
  }
}
