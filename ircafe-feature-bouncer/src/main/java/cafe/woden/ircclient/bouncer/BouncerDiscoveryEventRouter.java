package cafe.woden.ircclient.bouncer;

import cafe.woden.ircclient.bouncer.spi.BouncerBackendDiscoveryHandler;
import cafe.woden.ircclient.bouncer.spi.BouncerDiscoveredNetwork;
import java.util.List;
import java.util.Set;

/** Feature-owned routing of bouncer discovery events over already-resolved handlers. */
public final class BouncerDiscoveryEventRouter {

  private final BouncerDiscoveryHandlerCatalog handlerCatalog;

  private BouncerDiscoveryEventRouter(BouncerDiscoveryHandlerCatalog handlerCatalog) {
    this.handlerCatalog = handlerCatalog;
  }

  public static BouncerDiscoveryEventRouter fromHandlers(
      List<? extends BouncerBackendDiscoveryHandler> handlers) {
    return new BouncerDiscoveryEventRouter(BouncerDiscoveryHandlerCatalog.fromHandlers(handlers));
  }

  public Set<String> backendIds() {
    return handlerCatalog.backendIds();
  }

  /** Routes a discovered-network event when a matching handler exists. */
  public boolean routeNetworkDiscovered(BouncerDiscoveredNetwork network) {
    if (network == null) return false;
    return handlerCatalog
        .handler(network.backendId())
        .map(
            handler -> {
              handler.onNetworkDiscovered(network);
              return true;
            })
        .orElse(false);
  }

  /** Routes an origin-disconnected event when a matching handler exists. */
  public boolean routeOriginDisconnected(String backendId, String originServerId) {
    return handlerCatalog
        .handler(backendId)
        .map(
            handler -> {
              handler.onOriginDisconnected(originServerId);
              return true;
            })
        .orElse(false);
  }
}
