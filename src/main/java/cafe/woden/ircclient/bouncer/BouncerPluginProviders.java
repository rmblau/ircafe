package cafe.woden.ircclient.bouncer;

import cafe.woden.ircclient.bouncer.spi.BouncerBackendDiscoveryHandler;
import cafe.woden.ircclient.bouncer.spi.BouncerNetworkMappingStrategy;
import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.util.PluginServiceLoaderSupport;
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
      List<? extends BouncerNetworkMappingStrategy> springManagedStrategies,
      InstalledPluginsPort installedPlugins) {
    List<BouncerNetworkMappingStrategy> installedStrategies =
        installedPlugins == null
            ? List.of()
            : installedPlugins.loadInstalledServices(
                BouncerNetworkMappingStrategy.class, List.of());
    return BouncerPluginProviderCatalog.mappingStrategies(
        springManagedStrategies,
        applicationClasspathNetworkMappingStrategies(),
        installedStrategies);
  }

  static List<BouncerBackendDiscoveryHandler> backendDiscoveryHandlers(
      List<? extends BouncerBackendDiscoveryHandler> springManagedHandlers,
      InstalledPluginsPort installedPlugins) {
    List<BouncerBackendDiscoveryHandler> installedHandlers =
        installedPlugins == null
            ? List.of()
            : installedPlugins.loadInstalledServices(
                BouncerBackendDiscoveryHandler.class, List.of());
    return BouncerPluginProviderCatalog.discoveryHandlers(
        springManagedHandlers, installedHandlers);
  }

  private static List<BouncerNetworkMappingStrategy>
      applicationClasspathNetworkMappingStrategies() {
    return PluginServiceLoaderSupport.loadApplicationServices(
        BouncerNetworkMappingStrategy.class, BouncerPluginProviders.class);
  }
}
