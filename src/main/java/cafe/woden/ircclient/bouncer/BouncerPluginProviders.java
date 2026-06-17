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

  static List<cafe.woden.ircclient.bouncer.spi.BouncerNetworkMappingStrategy>
      networkMappingStrategies(
          List<? extends cafe.woden.ircclient.bouncer.spi.BouncerNetworkMappingStrategy>
              builtInStrategies,
          InstalledPluginsPort installedPlugins) {
    List<cafe.woden.ircclient.bouncer.spi.BouncerNetworkMappingStrategy> strategies =
        nonNullServices(builtInStrategies);
    if (installedPlugins == null) {
      return strategies;
    }
    return dedupeByProviderClass(
        installedPlugins.loadInstalledServices(
            cafe.woden.ircclient.bouncer.spi.BouncerNetworkMappingStrategy.class, strategies));
  }

  static List<cafe.woden.ircclient.bouncer.spi.BouncerBackendDiscoveryHandler>
      backendDiscoveryHandlers(
          List<? extends cafe.woden.ircclient.bouncer.spi.BouncerBackendDiscoveryHandler>
              builtInHandlers,
          InstalledPluginsPort installedPlugins) {
    List<cafe.woden.ircclient.bouncer.spi.BouncerBackendDiscoveryHandler> handlers =
        nonNullServices(builtInHandlers);
    if (installedPlugins == null) {
      return handlers;
    }
    return dedupeByProviderClass(
        installedPlugins.loadInstalledServices(
            cafe.woden.ircclient.bouncer.spi.BouncerBackendDiscoveryHandler.class, handlers));
  }

  private static <T> List<T> nonNullServices(List<? extends T> services) {
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

  private static <T> List<T> dedupeByProviderClass(List<? extends T> services) {
    java.util.LinkedHashSet<String> providerClassNames = new java.util.LinkedHashSet<>();
    ArrayList<T> deduped = new ArrayList<>();
    for (T service : nonNullServices(services)) {
      if (!providerClassNames.add(service.getClass().getName())) {
        continue;
      }
      deduped.add(service);
    }
    return List.copyOf(deduped);
  }
}
