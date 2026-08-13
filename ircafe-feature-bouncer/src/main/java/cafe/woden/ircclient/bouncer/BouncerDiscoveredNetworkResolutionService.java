package cafe.woden.ircclient.bouncer;

import cafe.woden.ircclient.bouncer.spi.BouncerDiscoveredNetwork;
import cafe.woden.ircclient.bouncer.spi.BouncerEphemeralServerSpec;
import cafe.woden.ircclient.bouncer.spi.BouncerNetworkMappingContext;
import cafe.woden.ircclient.bouncer.spi.BouncerNetworkMappingStrategy;
import cafe.woden.ircclient.bouncer.spi.BouncerServerProfile;
import cafe.woden.ircclient.bouncer.spi.ResolvedBouncerNetwork;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/** Feature-owned execution boundary for bouncer mapping strategy network resolution. */
public final class BouncerDiscoveredNetworkResolutionService {

  public BouncerDiscoveredNetworkResolution resolve(
      BouncerNetworkMappingStrategy mappingStrategy,
      BouncerServerProfile bouncerProfile,
      BouncerDiscoveredNetwork network,
      BouncerNetworkMappingContext mappingContext,
      Function<String, List<String>> autoJoinChannelsForServer) {
    Objects.requireNonNull(mappingStrategy, "mappingStrategy");
    Objects.requireNonNull(bouncerProfile, "bouncerProfile");
    Objects.requireNonNull(network, "network");
    Objects.requireNonNull(mappingContext, "mappingContext");
    Objects.requireNonNull(autoJoinChannelsForServer, "autoJoinChannelsForServer");

    ResolvedBouncerNetwork resolvedNetwork =
        Objects.requireNonNull(
            mappingStrategy.resolveNetwork(bouncerProfile, network, mappingContext),
            "resolvedNetwork");
    List<String> autoJoinChannels = autoJoinChannelsForServer.apply(resolvedNetwork.serverId());
    if (autoJoinChannels == null) {
      autoJoinChannels = List.of();
    }
    BouncerEphemeralServerSpec serverSpec =
        Objects.requireNonNull(
            mappingStrategy.buildEphemeralServer(bouncerProfile, resolvedNetwork, autoJoinChannels),
            "serverSpec");
    return new BouncerDiscoveredNetworkResolution(resolvedNetwork, serverSpec);
  }
}
