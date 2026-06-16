package cafe.woden.ircclient.bouncer;

import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import java.util.ArrayList;
import java.util.List;
import org.jmolecules.architecture.layered.ApplicationLayer;
import org.springframework.beans.factory.ObjectProvider;

/** Centralizes ServiceLoader-backed bouncer plugin provider handling. */
@ApplicationLayer
final class BouncerPluginProviders {
  private BouncerPluginProviders() {}

  static InstalledPluginsPort resolveInstalledPlugins(
      ObjectProvider<InstalledPluginsPort> installedPluginsProvider) {
    return installedPluginsProvider == null ? null : installedPluginsProvider.getIfAvailable();
  }

  static List<BouncerNetworkMappingStrategy> networkMappingStrategies(
      List<BouncerNetworkMappingStrategy> builtInStrategies,
      InstalledPluginsPort installedPlugins) {
    List<BouncerNetworkMappingStrategy> strategies = nonNullServices(builtInStrategies);
    if (installedPlugins == null) {
      return strategies;
    }
    return installedPlugins.loadInstalledServices(BouncerNetworkMappingStrategy.class, strategies);
  }

  static List<BouncerBackendDiscoveryHandler> backendDiscoveryHandlers(
      List<BouncerBackendDiscoveryHandler> builtInHandlers, InstalledPluginsPort installedPlugins) {
    List<BouncerBackendDiscoveryHandler> handlers = nonNullServices(builtInHandlers);
    if (installedPlugins == null) {
      return handlers;
    }
    return installedPlugins.loadInstalledServices(BouncerBackendDiscoveryHandler.class, handlers);
  }

  private static <T> List<T> nonNullServices(List<T> services) {
    if (services == null || services.isEmpty()) {
      return List.of();
    }
    ArrayList<T> resolved = new ArrayList<>(services.size());
    for (T service : services) {
      if (service != null) {
        resolved.add(service);
      }
    }
    return List.copyOf(resolved);
  }
}
