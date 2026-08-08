package cafe.woden.ircclient.bouncer;

import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.jmolecules.architecture.layered.ApplicationLayer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Registry of available bouncer backends and their metadata descriptors. */
@Component
@ApplicationLayer
public class BouncerBackendRegistry {

  private final BouncerBackendCatalog catalog;

  @Autowired
  public BouncerBackendRegistry(
      List<cafe.woden.ircclient.bouncer.spi.BouncerNetworkMappingStrategy> mappingStrategies,
      ObjectProvider<InstalledPluginsPort> installedPluginsProvider) {
    this(
        new ResolvedStrategies(
            BouncerPluginProviders.networkMappingStrategies(
                mappingStrategies,
                BouncerPluginProviders.resolveInstalledPlugins(installedPluginsProvider))));
  }

  public BouncerBackendRegistry(
      List<? extends cafe.woden.ircclient.bouncer.spi.BouncerNetworkMappingStrategy>
          mappingStrategies) {
    this(
        new ResolvedStrategies(
            BouncerPluginProviders.networkMappingStrategies(mappingStrategies, null)));
  }

  BouncerBackendRegistry(
      List<? extends cafe.woden.ircclient.bouncer.spi.BouncerNetworkMappingStrategy>
          mappingStrategies,
      InstalledPluginsPort installedPlugins) {
    this(
        new ResolvedStrategies(
            BouncerPluginProviders.networkMappingStrategies(mappingStrategies, installedPlugins)));
  }

  private BouncerBackendRegistry(ResolvedStrategies resolvedStrategies) {
    this.catalog = BouncerBackendCatalog.fromStrategies(resolvedStrategies.strategies());
  }

  public List<BouncerBackendDescriptor> descriptors() {
    return catalog.descriptors();
  }

  public Set<String> backendIds() {
    return catalog.backendIds();
  }

  public Optional<BouncerBackendDescriptor> find(String backendId) {
    return catalog.find(backendId);
  }

  public Optional<cafe.woden.ircclient.bouncer.spi.BouncerNetworkMappingStrategy> mappingStrategy(
      String backendId) {
    return catalog.mappingStrategy(backendId);
  }

  public cafe.woden.ircclient.bouncer.spi.BouncerNetworkMappingStrategy mappingStrategyOrMissing(
      String backendId) {
    return catalog.mappingStrategyOrMissing(backendId);
  }

  private record ResolvedStrategies(
      List<cafe.woden.ircclient.bouncer.spi.BouncerNetworkMappingStrategy> strategies) {
    private ResolvedStrategies {
      strategies = List.copyOf(Objects.requireNonNullElse(strategies, List.of()));
    }
  }
}
