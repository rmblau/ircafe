package cafe.woden.ircclient.bouncer;

import cafe.woden.ircclient.bouncer.spi.BouncerNetworkMappingStrategy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/** Feature-owned catalog of bouncer backend descriptors and mapping strategies. */
public final class BouncerBackendCatalog {

  private final List<BouncerBackendDescriptor> descriptors;
  private final Map<String, BouncerBackendDescriptor> byBackendId;
  private final Map<String, BouncerNetworkMappingStrategy> strategyByBackendId;
  private final BouncerMappingStrategySelector strategySelector =
      new BouncerMappingStrategySelector();

  private BouncerBackendCatalog(List<? extends BouncerNetworkMappingStrategy> mappingStrategies) {
    List<? extends BouncerNetworkMappingStrategy> source =
        mappingStrategies == null ? List.of() : mappingStrategies;
    ArrayList<BouncerNetworkMappingStrategy> strategies = new ArrayList<>();
    for (BouncerNetworkMappingStrategy strategy : source) {
      if (strategy != null) {
        strategies.add(strategy);
      }
    }

    ArrayList<BouncerBackendDescriptor> discovered = new ArrayList<>();
    for (BouncerNetworkMappingStrategy strategy : strategies) {
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

    LinkedHashMap<String, BouncerBackendDescriptor> descriptorsById = new LinkedHashMap<>();
    for (BouncerBackendDescriptor descriptor : discovered) {
      if (descriptor == null) continue;
      descriptorsById.putIfAbsent(descriptor.backendId(), descriptor);
    }

    LinkedHashMap<String, BouncerNetworkMappingStrategy> strategiesById = new LinkedHashMap<>();
    for (BouncerNetworkMappingStrategy strategy : strategies) {
      if (strategy == null) continue;
      String backend = normalize(strategy.backendId());
      if (backend == null || !descriptorsById.containsKey(backend)) continue;
      strategiesById.putIfAbsent(backend, strategy);
    }

    this.descriptors = List.copyOf(descriptorsById.values());
    this.byBackendId = Collections.unmodifiableMap(descriptorsById);
    this.strategyByBackendId = Collections.unmodifiableMap(strategiesById);
  }

  public static BouncerBackendCatalog fromStrategies(
      List<? extends BouncerNetworkMappingStrategy> mappingStrategies) {
    return new BouncerBackendCatalog(mappingStrategies);
  }

  public List<BouncerBackendDescriptor> descriptors() {
    return descriptors;
  }

  public Set<String> backendIds() {
    LinkedHashSet<String> out = new LinkedHashSet<>();
    for (BouncerBackendDescriptor descriptor : descriptors) {
      out.add(descriptor.backendId());
    }
    return Collections.unmodifiableSet(out);
  }

  public Optional<BouncerBackendDescriptor> find(String backendId) {
    return Optional.ofNullable(byBackendId.get(normalize(backendId)));
  }

  public Optional<BouncerNetworkMappingStrategy> mappingStrategy(String backendId) {
    return Optional.ofNullable(strategyByBackendId.get(normalize(backendId)));
  }

  public BouncerNetworkMappingStrategy mappingStrategyOrMissing(String backendId) {
    String normalizedBackendId = normalize(backendId);
    return strategySelector.select(
        normalizedBackendId, strategyByBackendId.get(normalizedBackendId));
  }

  private static String normalize(String value) {
    String v = Objects.toString(value, "").trim();
    return v.isEmpty() ? null : v.toLowerCase(Locale.ROOT);
  }
}
