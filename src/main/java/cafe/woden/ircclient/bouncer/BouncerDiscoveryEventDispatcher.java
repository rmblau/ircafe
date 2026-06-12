package cafe.woden.ircclient.bouncer;

import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import java.util.ArrayList;
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

  private final Map<String, BouncerBackendDiscoveryHandler> handlersByBackend;

  @Autowired
  public BouncerDiscoveryEventDispatcher(
      List<BouncerBackendDiscoveryHandler> handlers,
      ObjectProvider<InstalledPluginsPort> installedPluginsProvider) {
    this(loadInstalledHandlers(handlers, installedPluginsProvider));
  }

  public BouncerDiscoveryEventDispatcher(List<BouncerBackendDiscoveryHandler> handlers) {
    this(handlers, (InstalledPluginsPort) null);
  }

  BouncerDiscoveryEventDispatcher(
      List<BouncerBackendDiscoveryHandler> handlers, InstalledPluginsPort installedPlugins) {
    this(loadInstalledHandlers(handlers, installedPlugins));
  }

  private BouncerDiscoveryEventDispatcher(ResolvedHandlers resolvedHandlers) {
    List<BouncerBackendDiscoveryHandler> handlers = resolvedHandlers.handlers();
    HashMap<String, BouncerBackendDiscoveryHandler> map = new HashMap<>();
    for (BouncerBackendDiscoveryHandler handler : handlers) {
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
    BouncerBackendDiscoveryHandler handler = handlersByBackend.get(normalize(network.backendId()));
    if (handler == null) return;
    handler.onNetworkDiscovered(network);
  }

  @Override
  public void onOriginDisconnected(String backendId, String originServerId) {
    BouncerBackendDiscoveryHandler handler = handlersByBackend.get(normalize(backendId));
    if (handler == null) return;
    handler.onOriginDisconnected(originServerId);
  }

  private static ResolvedHandlers loadInstalledHandlers(
      List<BouncerBackendDiscoveryHandler> handlers,
      ObjectProvider<InstalledPluginsPort> installedPluginsProvider) {
    InstalledPluginsPort installedPlugins =
        installedPluginsProvider == null ? null : installedPluginsProvider.getIfAvailable();
    return loadInstalledHandlers(handlers, installedPlugins);
  }

  private static ResolvedHandlers loadInstalledHandlers(
      List<BouncerBackendDiscoveryHandler> handlers, InstalledPluginsPort installedPlugins) {
    List<BouncerBackendDiscoveryHandler> builtInHandlers = nonNullHandlers(handlers);
    if (installedPlugins == null) {
      return new ResolvedHandlers(builtInHandlers);
    }
    return new ResolvedHandlers(
        installedPlugins.loadInstalledServices(
            BouncerBackendDiscoveryHandler.class, builtInHandlers));
  }

  private static List<BouncerBackendDiscoveryHandler> nonNullHandlers(
      List<BouncerBackendDiscoveryHandler> handlers) {
    if (handlers == null || handlers.isEmpty()) {
      return List.of();
    }
    ArrayList<BouncerBackendDiscoveryHandler> resolved = new ArrayList<>(handlers.size());
    for (BouncerBackendDiscoveryHandler handler : handlers) {
      if (handler != null) {
        resolved.add(handler);
      }
    }
    return List.copyOf(resolved);
  }

  private record ResolvedHandlers(List<BouncerBackendDiscoveryHandler> handlers) {
    private ResolvedHandlers {
      handlers = List.copyOf(Objects.requireNonNullElse(handlers, List.of()));
    }
  }

  private static String normalize(String value) {
    String v = Objects.toString(value, "").trim();
    return v.isEmpty() ? null : v.toLowerCase(Locale.ROOT);
  }
}
