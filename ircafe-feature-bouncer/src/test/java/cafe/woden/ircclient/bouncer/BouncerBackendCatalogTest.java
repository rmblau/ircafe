package cafe.woden.ircclient.bouncer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.bouncer.spi.BouncerDiscoveredNetwork;
import cafe.woden.ircclient.bouncer.spi.BouncerNetworkMappingStrategy;
import cafe.woden.ircclient.bouncer.spi.BouncerServerProfile;
import cafe.woden.ircclient.bouncer.spi.ResolvedBouncerNetwork;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class BouncerBackendCatalogTest {

  @Test
  void buildsSortedDescriptorsFromMappingStrategies() {
    BouncerBackendCatalog catalog =
        BouncerBackendCatalog.fromStrategies(
            List.of(
                new FakeStrategy("znc", "znc:", "ZNC Networks", Set.of("znc.in/playback"), "z"),
                new FakeStrategy(
                    "soju", "soju:", "Soju Networks", Set.of("soju.im/bouncer-networks"), "s"),
                new FakeStrategy("generic", "bouncer:", "Bouncer Networks", Set.of(), "g")));

    assertEquals(Set.of("generic", "soju", "znc"), catalog.backendIds());
    assertEquals(
        List.of("generic", "soju", "znc"),
        catalog.descriptors().stream().map(BouncerBackendDescriptor::backendId).toList());

    BouncerBackendDescriptor generic = catalog.find("GENERIC").orElseThrow();
    assertEquals("bouncer:", generic.ephemeralIdPrefix());
    assertEquals("Bouncer Networks", generic.networksGroupLabel());

    BouncerBackendDescriptor soju = catalog.find("soju").orElseThrow();
    assertEquals(Set.of("soju.im/bouncer-networks"), soju.capabilityHints());
  }

  @Test
  void ignoresDuplicateBackendIdsAfterNormalizationAndKeepsFirstStrategy() {
    FakeStrategy first = new FakeStrategy("custom", "custom:", "Custom Networks", Set.of(), "one");
    FakeStrategy duplicate =
        new FakeStrategy(" CUSTOM ", "custom2:", "Custom Networks 2", Set.of("duplicate"), "two");

    BouncerBackendCatalog catalog = BouncerBackendCatalog.fromStrategies(List.of(first, duplicate));

    assertEquals(1, catalog.descriptors().size());
    assertEquals("custom:", catalog.find("custom").orElseThrow().ephemeralIdPrefix());
    assertEquals(first, catalog.mappingStrategy("CUSTOM").orElseThrow());
  }

  @Test
  void ignoresNullAndBlankStrategies() {
    FakeStrategy valid = new FakeStrategy("valid", "valid:", "Valid Networks", Set.of(), "ok");

    BouncerBackendCatalog catalog =
        BouncerBackendCatalog.fromStrategies(
            Arrays.asList(
                null, new FakeStrategy(" ", "blank:", "Blank", Set.of(), "blank"), valid));

    assertEquals(Set.of("valid"), catalog.backendIds());
    assertTrue(catalog.find("missing").isEmpty());
    assertTrue(catalog.mappingStrategy("missing").isEmpty());
  }

  @Test
  void selectsResolvedOrLazyMissingMappingStrategy() {
    FakeStrategy strategy =
        new FakeStrategy("soju", "soju:", "Soju Networks", Set.of(), "one");
    BouncerBackendCatalog catalog = BouncerBackendCatalog.fromStrategies(List.of(strategy));

    assertSame(strategy, catalog.mappingStrategyOrMissing(" SOJU "));

    BouncerNetworkMappingStrategy missing = catalog.mappingStrategyOrMissing(" MISSING ");
    assertEquals("missing", missing.backendId());
    IllegalStateException failure =
        assertThrows(
            IllegalStateException.class,
            () -> missing.resolveNetwork(null, null));
    assertEquals("Missing bouncer mapping strategy: missing", failure.getMessage());
  }

  private record FakeStrategy(
      String backendId,
      String ephemeralIdPrefix,
      String networksGroupLabel,
      Set<String> capabilityHints,
      String idSuffix)
      implements BouncerNetworkMappingStrategy {

    @Override
    public ResolvedBouncerNetwork resolveNetwork(
        BouncerServerProfile bouncer, BouncerDiscoveredNetwork network) {
      return new ResolvedBouncerNetwork(
          ephemeralIdPrefix + "origin:" + idSuffix, "user/" + idSuffix, "display", "display");
    }
  }
}
