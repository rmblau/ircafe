package cafe.woden.ircclient.bouncer.spi;

import java.util.List;
import java.util.Set;

/**
 * ServiceLoader-backed backend-specific mapping logic from discovery events to ephemeral server
 * specs.
 *
 * <p>Plugins register implementations in {@code
 * META-INF/services/cafe.woden.ircclient.bouncer.spi.BouncerNetworkMappingStrategy}.
 */
public interface BouncerNetworkMappingStrategy {

  String backendId();

  default String ephemeralIdPrefix() {
    return backendId() + ":";
  }

  default String networksGroupLabel() {
    return backendId() + " Networks";
  }

  default Set<String> capabilityHints() {
    return Set.of();
  }

  ResolvedBouncerNetwork resolveNetwork(
      BouncerServerProfile bouncer, BouncerDiscoveredNetwork network);

  default BouncerEphemeralServerSpec buildEphemeralServer(
      BouncerServerProfile bouncer,
      ResolvedBouncerNetwork resolved,
      List<String> autoJoinChannels) {
    return BouncerEphemeralServerSpec.from(resolved, autoJoinChannels);
  }

  default String networkDebugId(BouncerDiscoveredNetwork network) {
    return "";
  }
}
