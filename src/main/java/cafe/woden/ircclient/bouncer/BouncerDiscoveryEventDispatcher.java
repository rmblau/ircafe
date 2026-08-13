package cafe.woden.ircclient.bouncer;

import cafe.woden.ircclient.bouncer.spi.BouncerBackendDiscoveryHandler;
import cafe.woden.ircclient.bouncer.spi.BouncerDiscoveredNetwork;
import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import java.util.List;
import java.util.Objects;
import org.jmolecules.architecture.layered.ApplicationLayer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Routes generic discovery events to backend-specific handlers. */
@Component
@ApplicationLayer
public class BouncerDiscoveryEventDispatcher implements BouncerDiscoveryEventPort {

  private final BouncerDiscoveryEventRouter eventRouter;

  @Autowired
  public BouncerDiscoveryEventDispatcher(
      List<BouncerBackendDiscoveryHandler> handlers,
      ObjectProvider<InstalledPluginsPort> installedPluginsProvider) {
    this(
        new ResolvedHandlers(
            BouncerPluginProviders.backendDiscoveryHandlers(
                handlers,
                BouncerPluginProviders.resolveInstalledPlugins(installedPluginsProvider))));
  }

  public BouncerDiscoveryEventDispatcher(List<? extends BouncerBackendDiscoveryHandler> handlers) {
    this(new ResolvedHandlers(BouncerPluginProviders.backendDiscoveryHandlers(handlers, null)));
  }

  BouncerDiscoveryEventDispatcher(
      List<? extends BouncerBackendDiscoveryHandler> handlers,
      InstalledPluginsPort installedPlugins) {
    this(
        new ResolvedHandlers(
            BouncerPluginProviders.backendDiscoveryHandlers(handlers, installedPlugins)));
  }

  private BouncerDiscoveryEventDispatcher(ResolvedHandlers resolvedHandlers) {
    this.eventRouter = BouncerDiscoveryEventRouter.fromHandlers(resolvedHandlers.handlers());
  }

  @Override
  public void onNetworkDiscovered(BouncerDiscoveredNetwork network) {
    eventRouter.routeNetworkDiscovered(network);
  }

  @Override
  public void onOriginDisconnected(String backendId, String originServerId) {
    eventRouter.routeOriginDisconnected(backendId, originServerId);
  }

  private record ResolvedHandlers(List<BouncerBackendDiscoveryHandler> handlers) {
    private ResolvedHandlers {
      handlers = List.copyOf(Objects.requireNonNullElse(handlers, List.of()));
    }
  }
}
