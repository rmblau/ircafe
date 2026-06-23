package cafe.woden.ircclient.bouncer.spi;

/**
 * ServiceLoader-backed backend-specific handler for generic bouncer discovery events.
 *
 * <p>Plugins register implementations in {@code
 * META-INF/services/cafe.woden.ircclient.bouncer.spi.BouncerBackendDiscoveryHandler}.
 */
public interface BouncerBackendDiscoveryHandler {

  String backendId();

  void onNetworkDiscovered(BouncerDiscoveredNetwork network);

  void onOriginDisconnected(String originServerId);
}
