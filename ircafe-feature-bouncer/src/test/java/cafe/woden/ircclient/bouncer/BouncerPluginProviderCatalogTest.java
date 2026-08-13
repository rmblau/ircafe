package cafe.woden.ircclient.bouncer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cafe.woden.ircclient.bouncer.spi.BouncerBackendDiscoveryHandler;
import cafe.woden.ircclient.bouncer.spi.BouncerDiscoveredNetwork;
import cafe.woden.ircclient.bouncer.spi.BouncerNetworkMappingStrategy;
import java.util.List;
import org.junit.jupiter.api.Test;

class BouncerPluginProviderCatalogTest {

  @Test
  void mappingStrategiesPreserveSpringOrderAndPreferEarlierProviderGroups() {
    MappingStrategy springFirst = new MappingStrategy("spring-first");
    MappingStrategy springDuplicateClass = new MappingStrategy("spring-second");
    ApplicationMappingStrategy application = new ApplicationMappingStrategy("application");
    ApplicationMappingStrategy applicationDuplicate =
        new ApplicationMappingStrategy("application-duplicate");
    InstalledMappingStrategy installed = new InstalledMappingStrategy("installed");
    InstalledMappingStrategy installedDuplicate =
        new InstalledMappingStrategy("installed-duplicate");

    List<BouncerNetworkMappingStrategy> providers =
        BouncerPluginProviderCatalog.mappingStrategies(
            List.of(springFirst, springDuplicateClass),
            java.util.Arrays.asList(null, application, applicationDuplicate),
            List.of(installed, installedDuplicate, applicationDuplicate));

    assertEquals(List.of(springFirst, springDuplicateClass, application, installed), providers);
    assertThrows(UnsupportedOperationException.class, () -> providers.add(springFirst));
  }

  @Test
  void discoveryHandlersPreferSpringProvidersAndFilterNullInstalledEntries() {
    DiscoveryHandler springFirst = new DiscoveryHandler("spring-first");
    DiscoveryHandler springDuplicateClass = new DiscoveryHandler("spring-second");
    InstalledDiscoveryHandler installed = new InstalledDiscoveryHandler("installed");
    InstalledDiscoveryHandler installedDuplicate = new InstalledDiscoveryHandler("duplicate");

    List<BouncerBackendDiscoveryHandler> providers =
        BouncerPluginProviderCatalog.discoveryHandlers(
            List.of(springFirst, springDuplicateClass),
            java.util.Arrays.asList(null, installed, installedDuplicate, springFirst));

    assertEquals(List.of(springFirst, springDuplicateClass, installed), providers);
  }

  @Test
  void nullProviderGroupsProduceEmptyImmutableLists() {
    List<BouncerNetworkMappingStrategy> strategies =
        BouncerPluginProviderCatalog.mappingStrategies(null, null, null);
    List<BouncerBackendDiscoveryHandler> handlers =
        BouncerPluginProviderCatalog.discoveryHandlers(null, null);

    assertEquals(List.of(), strategies);
    assertEquals(List.of(), handlers);
    assertThrows(
        UnsupportedOperationException.class, () -> strategies.add(new MappingStrategy("x")));
    assertThrows(
        UnsupportedOperationException.class, () -> handlers.add(new DiscoveryHandler("x")));
  }

  private static class MappingStrategy implements BouncerNetworkMappingStrategy {
    private final String backendId;

    private MappingStrategy(String backendId) {
      this.backendId = backendId;
    }

    @Override
    public String backendId() {
      return backendId;
    }
  }

  private static final class ApplicationMappingStrategy extends MappingStrategy {
    private ApplicationMappingStrategy(String backendId) {
      super(backendId);
    }
  }

  private static final class InstalledMappingStrategy extends MappingStrategy {
    private InstalledMappingStrategy(String backendId) {
      super(backendId);
    }
  }

  private static class DiscoveryHandler implements BouncerBackendDiscoveryHandler {
    private final String backendId;

    private DiscoveryHandler(String backendId) {
      this.backendId = backendId;
    }

    @Override
    public String backendId() {
      return backendId;
    }

    @Override
    public void onNetworkDiscovered(BouncerDiscoveredNetwork network) {}

    @Override
    public void onOriginDisconnected(String originServerId) {}
  }

  private static final class InstalledDiscoveryHandler extends DiscoveryHandler {
    private InstalledDiscoveryHandler(String backendId) {
      super(backendId);
    }
  }
}
