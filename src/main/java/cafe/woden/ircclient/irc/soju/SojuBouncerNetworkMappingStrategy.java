package cafe.woden.ircclient.irc.soju;

import static cafe.woden.ircclient.util.Ircv3CapabilityNames.SOJU_BOUNCER_NETWORKS;

import cafe.woden.ircclient.bouncer.spi.BouncerDiscoveredNetwork;
import cafe.woden.ircclient.bouncer.spi.BouncerNetworkMappingStrategy;
import cafe.woden.ircclient.bouncer.spi.BouncerServerProfile;
import cafe.woden.ircclient.bouncer.spi.ResolvedBouncerNetwork;
import com.google.auto.service.AutoService;
import java.util.Set;
import org.jmolecules.architecture.layered.ApplicationLayer;
import org.springframework.stereotype.Component;

/** Soju-specific naming and login shaping strategy for bouncer discovery. */
@Component
@ApplicationLayer
@AutoService(BouncerNetworkMappingStrategy.class)
public class SojuBouncerNetworkMappingStrategy implements BouncerNetworkMappingStrategy {

  public static final String BACKEND_ID = "soju";
  public static final String NETWORKS_GROUP_LABEL = "Soju Networks";
  public static final String DISCOVERY_CAPABILITY = SOJU_BOUNCER_NETWORKS;

  @Override
  public String backendId() {
    return BACKEND_ID;
  }

  @Override
  public String ephemeralIdPrefix() {
    return SojuEphemeralNaming.EPHEMERAL_ID_PREFIX;
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
    SojuNetwork sojuNetwork =
        new SojuNetwork(
            network.originServerId(),
            network.networkId(),
            network.displayName(),
            network.attributes());
    SojuEphemeralNaming.Derived d = SojuEphemeralNaming.derive(bouncer, sojuNetwork);
    return new ResolvedBouncerNetwork(
        d.serverId(), d.loginUser(), d.networkName(), network.autoConnectName());
  }

  @Override
  public String networkDebugId(BouncerDiscoveredNetwork network) {
    return "netId=" + network.networkId();
  }
}
