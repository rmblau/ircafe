package cafe.woden.ircclient.irc.znc;

import cafe.woden.ircclient.bouncer.spi.BouncerDiscoveredNetwork;
import cafe.woden.ircclient.bouncer.spi.BouncerNetworkMappingStrategy;
import cafe.woden.ircclient.bouncer.spi.BouncerServerProfile;
import cafe.woden.ircclient.bouncer.spi.ResolvedBouncerNetwork;
import com.google.auto.service.AutoService;
import java.util.Set;

/** ZNC-specific naming and login shaping strategy for bouncer discovery. */
@AutoService(BouncerNetworkMappingStrategy.class)
public class ZncBouncerNetworkMappingStrategy implements BouncerNetworkMappingStrategy {

  public static final String BACKEND_ID = "znc";
  public static final String NETWORKS_GROUP_LABEL = "ZNC Networks";
  public static final String DISCOVERY_CAPABILITY = "znc.in/playback";

  @Override
  public String backendId() {
    return BACKEND_ID;
  }

  @Override
  public String ephemeralIdPrefix() {
    return ZncEphemeralNaming.EPHEMERAL_ID_PREFIX;
  }

  @Override
  public String networksGroupLabel() {
    return NETWORKS_GROUP_LABEL;
  }

  @Override
  public Set<String> capabilityHints() {
    return Set.of(DISCOVERY_CAPABILITY);
  }

  @Override
  public ResolvedBouncerNetwork resolveNetwork(
      BouncerServerProfile bouncer, BouncerDiscoveredNetwork network) {
    ZncNetwork zncNetwork = new ZncNetwork(network.originServerId(), network.displayName(), null);
    ZncEphemeralNaming.Derived d = ZncEphemeralNaming.derive(bouncer, zncNetwork);
    return new ResolvedBouncerNetwork(
        d.serverId(), d.loginUser(), network.displayName(), network.autoConnectName());
  }

  @Override
  public String networkDebugId(BouncerDiscoveredNetwork network) {
    return "networkId=" + network.networkId();
  }
}
