package cafe.woden.ircclient.irc.ircv3;

import cafe.woden.ircclient.config.api.InstalledPluginProblem;
import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.util.PluginServiceLoaderSupport;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.ToIntFunction;

/** Shared indexing and installed-provider fallback mechanics for typed IRCv3 runtime catalogs. */
final class Ircv3RuntimeProviderSupport {

  private Ircv3RuntimeProviderSupport() {}

  static <O extends Enum<O>, P> OperationIndex<O, P> indexByOperation(
      Class<O> operationType,
      List<? extends P> providers,
      Function<? super P, String> providerId,
      Function<? super P, ? extends Set<O>> operations,
      ToIntFunction<? super P> priority,
      String providerLabel) {
    Objects.requireNonNull(operationType, "operationType");
    Objects.requireNonNull(providerId, "providerId");
    Objects.requireNonNull(operations, "operations");
    Objects.requireNonNull(priority, "priority");

    EnumMap<O, P> indexed = new EnumMap<>(operationType);
    for (P provider : PluginServiceLoaderSupport.copyNonNullServices(providers)) {
      String normalizedId = requireProviderId(provider, providerId, providerLabel);
      Set<O> supportedOperations = operations.apply(provider);
      if (supportedOperations == null || supportedOperations.isEmpty()) {
        throw new IllegalStateException(
            providerLabel + " provider '" + normalizedId + "' reported no operations");
      }
      for (O operation : supportedOperations) {
        if (operation == null) {
          throw new IllegalStateException(
              providerLabel + " provider '" + normalizedId + "' reported a null operation");
        }
        P previous = indexed.get(operation);
        if (previous == null || priority.applyAsInt(provider) > priority.applyAsInt(previous)) {
          indexed.put(operation, provider);
          continue;
        }
        if (priority.applyAsInt(provider) == priority.applyAsInt(previous)) {
          throw new IllegalStateException(
              "Duplicate "
                  + providerLabel
                  + " provider for "
                  + operation
                  + " at priority "
                  + priority.applyAsInt(provider)
                  + ": "
                  + previous.getClass().getName()
                  + ", "
                  + provider.getClass().getName());
        }
      }
    }

    LinkedHashSet<String> providerIds = new LinkedHashSet<>();
    for (O operation : operationType.getEnumConstants()) {
      P provider = indexed.get(operation);
      if (provider != null) {
        providerIds.add(normalizeProviderId(providerId.apply(provider)));
      }
    }
    return new OperationIndex<>(Map.copyOf(indexed), List.copyOf(providerIds));
  }

  static <P> P selectHighestPriority(
      List<? extends P> providers,
      Function<? super P, String> providerId,
      ToIntFunction<? super P> priority,
      String providerLabel) {
    Objects.requireNonNull(providerId, "providerId");
    Objects.requireNonNull(priority, "priority");

    P selected = null;
    for (P candidate : PluginServiceLoaderSupport.copyNonNullServices(providers)) {
      requireProviderId(candidate, providerId, providerLabel);
      if (selected == null || priority.applyAsInt(candidate) > priority.applyAsInt(selected)) {
        selected = candidate;
        continue;
      }
      if (priority.applyAsInt(candidate) == priority.applyAsInt(selected)) {
        throw new IllegalStateException(
            "Duplicate "
                + providerLabel
                + " providers at priority "
                + priority.applyAsInt(candidate)
                + ": "
                + selected.getClass().getName()
                + ", "
                + candidate.getClass().getName());
      }
    }
    return selected;
  }

  static <P> List<P> loadApplicationProviders(Class<P> serviceType, Class<?> anchorType) {
    return PluginServiceLoaderSupport.loadApplicationServices(serviceType, anchorType);
  }

  static <P> List<P> loadInstalledProviders(
      Class<P> serviceType,
      Class<?> anchorType,
      InstalledPluginsPort installedPlugins,
      Function<List<P>, ?> validator,
      String problemSummary) {
    List<P> builtIns = loadApplicationProviders(serviceType, anchorType);
    if (installedPlugins == null) {
      return builtIns;
    }
    List<P> providers = installedPlugins.loadInstalledServices(serviceType, builtIns);
    try {
      validator.apply(providers);
      return PluginServiceLoaderSupport.copyNonNullServices(providers);
    } catch (RuntimeException error) {
      installedPlugins.recordPluginProblem(
          new InstalledPluginProblem(
              "ERROR",
              problemSummary,
              Objects.toString(error.getMessage(), error.getClass().getName())));
      return builtIns;
    }
  }

  static <T> List<T> copyRequired(List<? extends T> values) {
    return List.copyOf(values == null ? List.of() : values);
  }

  static <T> List<T> copyNonNull(List<? extends T> values) {
    return PluginServiceLoaderSupport.copyNonNullServices(values);
  }

  static String normalizeProviderId(String providerId) {
    return Objects.toString(providerId, "").trim().toLowerCase(Locale.ROOT);
  }

  private static <P> String requireProviderId(
      P provider, Function<? super P, String> providerId, String providerLabel) {
    String normalizedId = normalizeProviderId(providerId.apply(provider));
    if (normalizedId.isEmpty()) {
      throw new IllegalStateException(
          providerLabel
              + " provider reported a blank provider id: "
              + provider.getClass().getName());
    }
    return normalizedId;
  }

  record OperationIndex<O extends Enum<O>, P>(
      Map<O, P> providersByOperation, List<String> providerIds) {

    OperationIndex {
      providersByOperation = Map.copyOf(providersByOperation);
      providerIds = List.copyOf(providerIds);
    }

    P provider(O operation) {
      return operation == null ? null : providersByOperation.get(operation);
    }

    boolean supports(O operation) {
      return provider(operation) != null;
    }
  }
}
