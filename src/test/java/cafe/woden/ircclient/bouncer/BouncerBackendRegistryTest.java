package cafe.woden.ircclient.bouncer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.bouncer.spi.BouncerDiscoveredNetwork;
import cafe.woden.ircclient.bouncer.spi.BouncerNetworkMappingStrategy;
import cafe.woden.ircclient.bouncer.spi.BouncerServerProfile;
import cafe.woden.ircclient.bouncer.spi.ResolvedBouncerNetwork;
import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.junit.jupiter.api.Test;

class BouncerBackendRegistryTest {

  @Test
  void buildsDescriptorsFromMappingStrategies() {
    BouncerBackendRegistry registry =
        new BouncerBackendRegistry(
            List.of(
                new FakeStrategy("znc", "znc:", "ZNC Networks", Set.of("znc.in/playback"), "net-z"),
                new FakeStrategy(
                    "soju", "soju:", "Soju Networks", Set.of("soju.im/bouncer-networks"), "net-s"),
                new FakeStrategy("generic", "bouncer:", "Bouncer Networks", Set.of(), "net-g")));

    assertEquals(Set.of("generic", "soju", "znc"), registry.backendIds());
    assertEquals(3, registry.descriptors().size());

    BouncerBackendDescriptor generic = registry.find("GENERIC").orElseThrow();
    assertEquals("bouncer:", generic.ephemeralIdPrefix());
    assertEquals("Bouncer Networks", generic.networksGroupLabel());

    BouncerBackendDescriptor soju = registry.find("soju").orElseThrow();
    assertEquals(Set.of("soju.im/bouncer-networks"), soju.capabilityHints());
  }

  @Test
  void ignoresDuplicateBackendIdsAfterNormalization() {
    BouncerBackendRegistry registry =
        new BouncerBackendRegistry(
            List.of(
                new FakeStrategy("custom", "custom:", "Custom Networks", Set.of(), "one"),
                new FakeStrategy(" CUSTOM ", "custom:", "Custom Networks 2", Set.of(), "two")));

    assertEquals(
        1,
        registry.descriptors().stream()
            .filter(descriptor -> descriptor.backendId().equals("custom"))
            .count());
    assertTrue(registry.find("custom").isPresent());
  }

  @Test
  void exposesMappingStrategiesByBackendId() {
    FakeStrategy first = new FakeStrategy("soju", "soju:", "Soju Networks", Set.of(), "one");
    FakeStrategy duplicate =
        new FakeStrategy(" SOJU ", "soju:", "Soju Networks 2", Set.of(), "two");
    FakeStrategy znc = new FakeStrategy("znc", "znc:", "ZNC Networks", Set.of(), "three");

    BouncerBackendRegistry registry = new BouncerBackendRegistry(List.of(first, duplicate, znc));

    assertEquals(first, registry.mappingStrategy("SOJU").orElseThrow());
    assertEquals(znc, registry.mappingStrategy("znc").orElseThrow());
    assertTrue(registry.mappingStrategy("missing").isEmpty());
  }

  @Test
  void loadsMappingStrategiesFromInstalledPluginsPort() {
    BouncerBackendRegistry registry =
        new BouncerBackendRegistry(
            List.of(new FakeStrategy("generic", "bouncer:", "Bouncer Networks", Set.of(), "net-g")),
            new FakeInstalledPluginsPort(
                List.of(
                    new FakePluginStrategy(
                        "plugin-bouncer",
                        "plugin:",
                        "Plugin Bouncer Networks",
                        Set.of("example.com/plugin-bouncer"),
                        "net-p"))));

    assertTrue(registry.backendIds().containsAll(Set.of("generic", "plugin-bouncer")));

    BouncerBackendDescriptor plugin = registry.find("PLUGIN-BOUNCER").orElseThrow();
    assertEquals("plugin:", plugin.ephemeralIdPrefix());
    assertEquals("Plugin Bouncer Networks", plugin.networksGroupLabel());
    assertEquals(Set.of("example.com/plugin-bouncer"), plugin.capabilityHints());
  }

  @Test
  void loadsMappingStrategySpiFromInstalledPluginsPort() {
    BouncerBackendRegistry registry =
        new BouncerBackendRegistry(
            List.of(new FakeStrategy("generic", "bouncer:", "Bouncer Networks", Set.of(), "net-g")),
            new FakeInstalledPluginsPort(
                List.of(
                    new FakeSpiStrategy(
                        "plugin-spi",
                        "plugin-spi:",
                        "Plugin SPI Networks",
                        Set.of("example.com/plugin-spi"),
                        "net-spi"))));

    assertTrue(registry.backendIds().containsAll(Set.of("generic", "plugin-spi")));

    BouncerBackendDescriptor plugin = registry.find("PLUGIN-SPI").orElseThrow();
    assertEquals("plugin-spi:", plugin.ephemeralIdPrefix());
    assertEquals("Plugin SPI Networks", plugin.networksGroupLabel());
    assertEquals(Set.of("example.com/plugin-spi"), plugin.capabilityHints());
  }

  @Test
  void loadsNoArgBuiltInMappingStrategiesThroughClasspathServiceLoader() {
    BouncerBackendRegistry registry = new BouncerBackendRegistry(List.of());

    assertTrue(registry.backendIds().containsAll(Set.of("generic", "soju", "znc")));
  }

  @Test
  void selectsResolvedOrLazyMissingMappingStrategy() {
    FakeStrategy strategy =
        new FakeStrategy("custom", "custom:", "Custom Networks", Set.of(), "one");
    BouncerBackendRegistry registry = new BouncerBackendRegistry(List.of(strategy));

    assertSame(strategy, registry.mappingStrategyOrMissing(" CUSTOM "));

    BouncerNetworkMappingStrategy missing = registry.mappingStrategyOrMissing(" MISSING ");
    assertEquals("missing", missing.backendId());
    IllegalStateException failure =
        assertThrows(IllegalStateException.class, () -> missing.resolveNetwork(null, null));
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

  private record FakeSpiStrategy(
      String backendId,
      String ephemeralIdPrefix,
      String networksGroupLabel,
      Set<String> capabilityHints,
      String idSuffix)
      implements cafe.woden.ircclient.bouncer.spi.BouncerNetworkMappingStrategy {

    @Override
    public ResolvedBouncerNetwork resolveNetwork(
        BouncerServerProfile bouncer, BouncerDiscoveredNetwork network) {
      return new ResolvedBouncerNetwork(
          ephemeralIdPrefix + "origin:" + idSuffix, "user/" + idSuffix, "display", "display");
    }
  }

  private record FakePluginStrategy(
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

  private static final class FakeInstalledPluginsPort implements InstalledPluginsPort {
    private final List<?> pluginServices;

    private FakeInstalledPluginsPort(List<?> pluginServices) {
      this.pluginServices = List.copyOf(pluginServices);
    }

    @Override
    public <T> List<T> loadInstalledServices(Class<T> serviceType, List<T> builtInServices) {
      ArrayList<T> services =
          new ArrayList<>(Objects.requireNonNullElse(builtInServices, List.of()));
      for (Object service : pluginServices) {
        if (serviceType.isInstance(service)) {
          services.add(serviceType.cast(service));
        }
      }
      return List.copyOf(services);
    }
  }
}
