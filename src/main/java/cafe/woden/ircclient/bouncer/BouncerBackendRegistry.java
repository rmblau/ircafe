package cafe.woden.ircclient.bouncer;

import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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

  private final List<BouncerBackendDescriptor> descriptors;
  private final Map<String, BouncerBackendDescriptor> byBackendId;

  @Autowired
  public BouncerBackendRegistry(
      List<BouncerNetworkMappingStrategy> mappingStrategies,
      ObjectProvider<InstalledPluginsPort> installedPluginsProvider) {
    this(
        new ResolvedStrategies(
            BouncerPluginProviders.networkMappingStrategies(
                mappingStrategies,
                BouncerPluginProviders.resolveInstalledPlugins(installedPluginsProvider))));
  }

  public BouncerBackendRegistry(List<BouncerNetworkMappingStrategy> mappingStrategies) {
    this(
        new ResolvedStrategies(
            BouncerPluginProviders.networkMappingStrategies(mappingStrategies, null)));
  }

  BouncerBackendRegistry(
      List<BouncerNetworkMappingStrategy> mappingStrategies,
      InstalledPluginsPort installedPlugins) {
    this(
        new ResolvedStrategies(
            BouncerPluginProviders.networkMappingStrategies(mappingStrategies, installedPlugins)));
  }

  private BouncerBackendRegistry(ResolvedStrategies resolvedStrategies) {
    List<BouncerNetworkMappingStrategy> mappingStrategies = resolvedStrategies.strategies();

    ArrayList<BouncerBackendDescriptor> discovered = new ArrayList<>();
    for (BouncerNetworkMappingStrategy strategy : mappingStrategies) {
      if (strategy == null) continue;
      String backend = normalize(strategy.backendId());
      if (backend == null) continue;
      discovered.add(
          new BouncerBackendDescriptor(
              backend,
              strategy.ephemeralIdPrefix(),
              strategy.networksGroupLabel(),
              strategy.capabilityHints()));
    }

    discovered.sort(java.util.Comparator.comparing(BouncerBackendDescriptor::backendId));

    LinkedHashMap<String, BouncerBackendDescriptor> map = new LinkedHashMap<>();
    for (BouncerBackendDescriptor descriptor : discovered) {
      if (descriptor == null) continue;
      map.putIfAbsent(descriptor.backendId(), descriptor);
    }

    this.descriptors = List.copyOf(map.values());
    this.byBackendId = java.util.Collections.unmodifiableMap(map);
  }

  public List<BouncerBackendDescriptor> descriptors() {
    return descriptors;
  }

  public Set<String> backendIds() {
    LinkedHashSet<String> out = new LinkedHashSet<>();
    for (BouncerBackendDescriptor descriptor : descriptors) {
      out.add(descriptor.backendId());
    }
    return java.util.Collections.unmodifiableSet(out);
  }

  public Optional<BouncerBackendDescriptor> find(String backendId) {
    return Optional.ofNullable(byBackendId.get(normalize(backendId)));
  }

  private record ResolvedStrategies(List<BouncerNetworkMappingStrategy> strategies) {
    private ResolvedStrategies {
      strategies = List.copyOf(Objects.requireNonNullElse(strategies, List.of()));
    }
  }

  private static String normalize(String value) {
    String v = Objects.toString(value, "").trim();
    return v.isEmpty() ? null : v.toLowerCase(Locale.ROOT);
  }
}
