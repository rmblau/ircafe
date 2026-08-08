package cafe.woden.ircclient.bouncer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.bouncer.spi.BouncerDiscoveredNetwork;
import cafe.woden.ircclient.bouncer.spi.BouncerEphemeralServerSpec;
import cafe.woden.ircclient.bouncer.spi.BouncerNetworkMappingContext;
import cafe.woden.ircclient.bouncer.spi.BouncerNetworkMappingStrategy;
import cafe.woden.ircclient.bouncer.spi.BouncerServerProfile;
import cafe.woden.ircclient.bouncer.spi.ResolvedBouncerNetwork;
import cafe.woden.ircclient.config.api.RuntimeConfigPathPort;
import cafe.woden.ircclient.config.plugins.InstalledPluginServices;
import cafe.woden.ircclient.config.plugins.InstalledPluginServicesTestSupport;
import cafe.woden.ircclient.util.CompiledPluginJarSupport;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BouncerNetworkMappingStrategyGuideFixtureTest {

  private static final String GUIDE_PROVIDER_CLASS =
      "example.bouncer.ExampleBouncerMappingStrategy";

  @TempDir Path tempDir;

  @Test
  void documentedExampleMappingStrategyLoadsThroughInstalledPluginRegistry() throws Exception {
    Path runtimeConfigDirectory = Files.createDirectories(tempDir.resolve("config-home/ircafe"));
    Path pluginDir = Files.createDirectories(runtimeConfigDirectory.resolve("plugins"));
    CompiledPluginJarSupport.writePluginJar(
        pluginDir.resolve("bouncer-mapping-guide-example.jar"),
        GUIDE_PROVIDER_CLASS,
        guideProviderSource(),
        BouncerNetworkMappingStrategy.class.getName(),
        CompiledPluginJarSupport.compatibleManifest("bouncer-mapping-guide-example", "1.0.0"));
    RuntimeConfigPathPort runtimeConfigPathPort =
        () -> runtimeConfigDirectory.resolve("ircafe.yml");

    InstalledPluginServices installedPlugins = new InstalledPluginServices(runtimeConfigPathPort);
    try {
      BouncerBackendRegistry registry = new BouncerBackendRegistry(List.of(), installedPlugins);

      assertTrue(installedPlugins.pluginProblems().isEmpty());
      assertTrue(registry.backendIds().contains("example-bouncer"));

      BouncerBackendDescriptor descriptor = registry.find(" EXAMPLE-BOUNCER ").orElseThrow();
      assertEquals("example:", descriptor.ephemeralIdPrefix());
      assertEquals("Example Bouncer Networks", descriptor.networksGroupLabel());
      assertEquals(Set.of("example.com/bouncer-networks"), descriptor.capabilityHints());

      BouncerNetworkMappingStrategy strategy =
          registry.mappingStrategy("example-bouncer").orElseThrow();
      BouncerServerProfile profile =
          new BouncerServerProfile("origin-1", "login-user", "sasl-user");
      BouncerDiscoveredNetwork network =
          new BouncerDiscoveredNetwork(
              "example-bouncer",
              "origin-1",
              "libera",
              "Libera Chat",
              "Libera Auto",
              "hint-user",
              Set.of("NETWORKS"),
              Map.of());
      BouncerNetworkMappingContext context =
          new BouncerNetworkMappingContext("{base}@{network}", true);

      ResolvedBouncerNetwork resolved = strategy.resolveNetwork(profile, network, context);
      BouncerEphemeralServerSpec serverSpec =
          strategy.buildEphemeralServer(
              profile, resolved, List.of("#ircafe", " ", "#plugins"));

      assertEquals("example:origin-1:libera", resolved.serverId());
      assertEquals("hint-user@libera", resolved.loginUser());
      assertEquals("Libera Chat", resolved.displayName());
      assertEquals("Libera Auto", resolved.autoConnectName());
      assertEquals("example:origin-1:libera", serverSpec.serverId());
      assertEquals("hint-user@libera", serverSpec.loginUser());
      assertEquals(List.of("#ircafe", "#plugins"), serverSpec.autoJoinChannels());
    } finally {
      InstalledPluginServicesTestSupport.shutdown(installedPlugins);
    }
  }

  private static String guideProviderSource() {
    return """
        package example.bouncer;

        import cafe.woden.ircclient.bouncer.spi.BouncerDiscoveredNetwork;
        import cafe.woden.ircclient.bouncer.spi.BouncerEphemeralServerSpec;
        import cafe.woden.ircclient.bouncer.spi.BouncerNetworkMappingContext;
        import cafe.woden.ircclient.bouncer.spi.BouncerNetworkMappingStrategy;
        import cafe.woden.ircclient.bouncer.spi.BouncerServerProfile;
        import cafe.woden.ircclient.bouncer.spi.ResolvedBouncerNetwork;
        import java.util.List;
        import java.util.Set;

        public final class ExampleBouncerMappingStrategy implements BouncerNetworkMappingStrategy {
          @Override
          public String backendId() {
            return " Example-Bouncer ";
          }

          @Override
          public String ephemeralIdPrefix() {
            return "example:";
          }

          @Override
          public String networksGroupLabel() {
            return "Example Bouncer Networks";
          }

          @Override
          public Set<String> capabilityHints() {
            return Set.of("example.com/bouncer-networks");
          }

          @Override
          public ResolvedBouncerNetwork resolveNetwork(
              BouncerServerProfile bouncer,
              BouncerDiscoveredNetwork network,
              BouncerNetworkMappingContext context) {
            String baseLogin =
                context.preferLoginHint() ? network.loginUserHint() : bouncer.preferredLoginUser();
            if (baseLogin == null || baseLogin.isBlank()) {
              baseLogin = "guest";
            }
            String loginUser =
                context
                    .genericLoginTemplate()
                    .replace("{base}", baseLogin)
                    .replace("{network}", network.networkId());
            String serverId = "example:" + network.originServerId() + ":" + network.networkId();
            return new ResolvedBouncerNetwork(
                serverId, loginUser, network.displayName(), network.autoConnectName());
          }

          @Override
          public BouncerEphemeralServerSpec buildEphemeralServer(
              BouncerServerProfile bouncer,
              ResolvedBouncerNetwork resolved,
              List<String> autoJoinChannels) {
            List<String> channels =
                autoJoinChannels.stream()
                    .filter(channel -> channel != null && !channel.isBlank())
                    .toList();
            return new BouncerEphemeralServerSpec(
                resolved.serverId(), resolved.loginUser(), channels);
          }
        }
        """;
  }
}
