package cafe.woden.ircclient.bouncer;

import org.jmolecules.architecture.layered.ApplicationLayer;

/**
 * ServiceLoader-backed backend-specific handler for generic bouncer discovery events.
 *
 * <p>Plugins register implementations in {@code
 * META-INF/services/cafe.woden.ircclient.bouncer.BouncerBackendDiscoveryHandler}.
 */
@ApplicationLayer
public interface BouncerBackendDiscoveryHandler {

  String backendId();

  void onNetworkDiscovered(BouncerDiscoveredNetwork network);

  void onOriginDisconnected(String originServerId);
}
