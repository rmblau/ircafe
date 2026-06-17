package cafe.woden.ircclient.bouncer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.config.IrcProperties;
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
                new FakeStrategy("soju", "soju:", "Soju Networks", Set.of(), "one"),
                new FakeStrategy(" SOJU ", "soju:", "Soju Networks 2", Set.of(), "two")));

    assertEquals(1, registry.descriptors().size());
    assertTrue(registry.find("soju").isPresent());
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

    assertEquals(Set.of("generic", "plugin-bouncer"), registry.backendIds());

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

    assertEquals(Set.of("generic", "plugin-spi"), registry.backendIds());

    BouncerBackendDescriptor plugin = registry.find("PLUGIN-SPI").orElseThrow();
    assertEquals("plugin-spi:", plugin.ephemeralIdPrefix());
    assertEquals("Plugin SPI Networks", plugin.networksGroupLabel());
    assertEquals(Set.of("example.com/plugin-spi"), plugin.capabilityHints());
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
        IrcProperties.Server bouncer, BouncerDiscoveredNetwork network) {
      return new ResolvedBouncerNetwork(
          ephemeralIdPrefix + "origin:" + idSuffix, "user/" + idSuffix, "display", "display");
    }

    @Override
    public IrcProperties.Server buildEphemeralServer(
        IrcProperties.Server bouncer,
        ResolvedBouncerNetwork resolved,
        List<String> autoJoinChannels) {
      return bouncer;
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
        IrcProperties.Server bouncer, BouncerDiscoveredNetwork network) {
      return new ResolvedBouncerNetwork(
          ephemeralIdPrefix + "origin:" + idSuffix, "user/" + idSuffix, "display", "display");
    }

    @Override
    public IrcProperties.Server buildEphemeralServer(
        IrcProperties.Server bouncer,
        ResolvedBouncerNetwork resolved,
        List<String> autoJoinChannels) {
      return bouncer;
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
        IrcProperties.Server bouncer, BouncerDiscoveredNetwork network) {
      return new ResolvedBouncerNetwork(
          ephemeralIdPrefix + "origin:" + idSuffix, "user/" + idSuffix, "display", "display");
    }

    @Override
    public IrcProperties.Server buildEphemeralServer(
        IrcProperties.Server bouncer,
        ResolvedBouncerNetwork resolved,
        List<String> autoJoinChannels) {
      return bouncer;
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
