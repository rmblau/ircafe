package cafe.woden.ircclient.bouncer;

import cafe.woden.ircclient.bouncer.spi.BouncerDiscoveredNetwork;
import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.jmolecules.architecture.layered.ApplicationLayer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Routes generic discovery events to backend-specific handlers. */
@Component
@ApplicationLayer
public class BouncerDiscoveryEventDispatcher implements BouncerDiscoveryEventPort {

  private final Map<String, cafe.woden.ircclient.bouncer.spi.BouncerBackendDiscoveryHandler>
      handlersByBackend;

  @Autowired
  public BouncerDiscoveryEventDispatcher(
      List<cafe.woden.ircclient.bouncer.spi.BouncerBackendDiscoveryHandler> handlers,
      ObjectProvider<InstalledPluginsPort> installedPluginsProvider) {
    this(
        new ResolvedHandlers(
            BouncerPluginProviders.backendDiscoveryHandlers(
                handlers,
                BouncerPluginProviders.resolveInstalledPlugins(installedPluginsProvider))));
  }

  public BouncerDiscoveryEventDispatcher(
      List<? extends cafe.woden.ircclient.bouncer.spi.BouncerBackendDiscoveryHandler> handlers) {
    this(new ResolvedHandlers(BouncerPluginProviders.backendDiscoveryHandlers(handlers, null)));
  }

  BouncerDiscoveryEventDispatcher(
      List<? extends cafe.woden.ircclient.bouncer.spi.BouncerBackendDiscoveryHandler> handlers,
      InstalledPluginsPort installedPlugins) {
    this(
        new ResolvedHandlers(
            BouncerPluginProviders.backendDiscoveryHandlers(handlers, installedPlugins)));
  }

  private BouncerDiscoveryEventDispatcher(ResolvedHandlers resolvedHandlers) {
    List<cafe.woden.ircclient.bouncer.spi.BouncerBackendDiscoveryHandler> handlers =
        resolvedHandlers.handlers();
    HashMap<String, cafe.woden.ircclient.bouncer.spi.BouncerBackendDiscoveryHandler> map =
        new HashMap<>();
    for (cafe.woden.ircclient.bouncer.spi.BouncerBackendDiscoveryHandler handler : handlers) {
      if (handler == null) continue;
      String backend = normalize(handler.backendId());
      if (backend == null) continue;
      map.putIfAbsent(backend, handler);
    }
    this.handlersByBackend = Map.copyOf(map);
  }

  @Override
  public void onNetworkDiscovered(BouncerDiscoveredNetwork network) {
    if (network == null) return;
    cafe.woden.ircclient.bouncer.spi.BouncerBackendDiscoveryHandler handler =
        handlersByBackend.get(normalize(network.backendId()));
    if (handler == null) return;
    handler.onNetworkDiscovered(network);
  }

  @Override
  public void onOriginDisconnected(String backendId, String originServerId) {
    cafe.woden.ircclient.bouncer.spi.BouncerBackendDiscoveryHandler handler =
        handlersByBackend.get(normalize(backendId));
    if (handler == null) return;
    handler.onOriginDisconnected(originServerId);
  }

  private record ResolvedHandlers(
      List<cafe.woden.ircclient.bouncer.spi.BouncerBackendDiscoveryHandler> handlers) {
    private ResolvedHandlers {
      handlers = List.copyOf(Objects.requireNonNullElse(handlers, List.of()));
    }
  }

  private static String normalize(String value) {
    String v = Objects.toString(value, "").trim();
    return v.isEmpty() ? null : v.toLowerCase(Locale.ROOT);
  }
}
