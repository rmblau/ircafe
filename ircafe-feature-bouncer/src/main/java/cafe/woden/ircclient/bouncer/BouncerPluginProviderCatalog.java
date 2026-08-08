package cafe.woden.ircclient.bouncer;

import cafe.woden.ircclient.bouncer.spi.BouncerBackendDiscoveryHandler;
import cafe.woden.ircclient.bouncer.spi.BouncerNetworkMappingStrategy;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Pure provider-list composition policy for bouncer extension points. */
public final class BouncerPluginProviderCatalog {

  private BouncerPluginProviderCatalog() {}

  /**
   * Combines Spring-managed, application-classpath, and installed-plugin mapping strategies.
   *
   * <p>Spring-managed entries retain their declared order and multiplicity. Later provider groups
   * contribute only the first provider for each implementation class, so application providers
   * override matching installed-plugin providers.
   */
  public static List<BouncerNetworkMappingStrategy> mappingStrategies(
      List<? extends BouncerNetworkMappingStrategy> springManagedStrategies,
      List<? extends BouncerNetworkMappingStrategy> applicationClasspathStrategies,
      List<? extends BouncerNetworkMappingStrategy> installedPluginStrategies) {
    return appendDedupeByProviderClass(
        springManagedStrategies, applicationClasspathStrategies, installedPluginStrategies);
  }

  /**
   * Combines Spring-managed and installed-plugin discovery handlers.
   *
   * <p>Spring-managed entries retain their declared order and multiplicity. Installed providers
   * whose implementation class is already represented are ignored.
   */
  public static List<BouncerBackendDiscoveryHandler> discoveryHandlers(
      List<? extends BouncerBackendDiscoveryHandler> springManagedHandlers,
      List<? extends BouncerBackendDiscoveryHandler> installedPluginHandlers) {
    return appendDedupeByProviderClass(springManagedHandlers, installedPluginHandlers);
  }

  @SafeVarargs
  private static <T> List<T> appendDedupeByProviderClass(
      List<? extends T> baseProviders, List<? extends T>... appendedProviderGroups) {
    ArrayList<T> providers = new ArrayList<>();
    Set<String> providerClassNames = new LinkedHashSet<>();

    if (baseProviders != null) {
      for (T provider : baseProviders) {
        if (provider == null) continue;
        providers.add(provider);
        providerClassNames.add(provider.getClass().getName());
      }
    }

    if (appendedProviderGroups != null) {
      for (List<? extends T> providerGroup : appendedProviderGroups) {
        if (providerGroup == null) continue;
        for (T provider : providerGroup) {
          if (provider == null) continue;
          if (!providerClassNames.add(provider.getClass().getName())) continue;
          providers.add(provider);
        }
      }
    }

    return List.copyOf(providers);
  }
}
