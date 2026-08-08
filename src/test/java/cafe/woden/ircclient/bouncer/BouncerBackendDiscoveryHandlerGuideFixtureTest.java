package cafe.woden.ircclient.bouncer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.bouncer.spi.BouncerBackendDiscoveryHandler;
import cafe.woden.ircclient.bouncer.spi.BouncerDiscoveredNetwork;
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

class BouncerBackendDiscoveryHandlerGuideFixtureTest {

  private static final String GUIDE_PROVIDER_CLASS =
      "example.bouncer.ExampleBouncerDiscoveryHandler";

  @TempDir Path tempDir;

  @Test
  void documentedDiscoveryHandlerReceivesOnlyMatchingBouncerLifecycleEvents() throws Exception {
    Path runtimeConfigDirectory = Files.createDirectories(tempDir.resolve("config-home/ircafe"));
    Path pluginDir = Files.createDirectories(runtimeConfigDirectory.resolve("plugins"));
    CompiledPluginJarSupport.writePluginJar(
        pluginDir.resolve("bouncer-discovery-handler-guide-example.jar"),
        GUIDE_PROVIDER_CLASS,
        guideProviderSource(),
        BouncerBackendDiscoveryHandler.class.getName(),
        CompiledPluginJarSupport.compatibleManifest(
            "bouncer-discovery-handler-guide-example", "1.0.0"));
    RuntimeConfigPathPort runtimeConfigPathPort =
        () -> runtimeConfigDirectory.resolve("ircafe.yml");
    Path discoveredMarker = tempDir.resolve("discovered.txt");
    Path disconnectedMarker = tempDir.resolve("disconnected.txt");

    System.setProperty("ircafe.test.bouncerDiscoveryMarker", discoveredMarker.toString());
    System.setProperty("ircafe.test.bouncerDisconnectMarker", disconnectedMarker.toString());
    InstalledPluginServices installedPlugins = new InstalledPluginServices(runtimeConfigPathPort);
    try {
      BouncerDiscoveryEventDispatcher dispatcher =
          new BouncerDiscoveryEventDispatcher(List.of(), installedPlugins);

      dispatcher.onNetworkDiscovered(
          new BouncerDiscoveredNetwork(
              "other-bouncer", "origin-0", "other", "Other", "Other", Map.of()));
      assertFalse(Files.exists(discoveredMarker));

      dispatcher.onNetworkDiscovered(
          new BouncerDiscoveredNetwork(
              "GUIDE-BOUNCER",
              "origin-1",
              "libera",
              "Libera Chat",
              "Libera Auto",
              "login-user",
              Set.of("BNC", "NETWORKS"),
              Map.of("plan", "test")));
      dispatcher.onOriginDisconnected(" guide-bouncer ", "origin-1");

      assertTrue(installedPlugins.pluginProblems().isEmpty());
      assertEquals(
          "origin-1|libera|Libera Chat|login-user|true|test", Files.readString(discoveredMarker));
      assertEquals("origin-1", Files.readString(disconnectedMarker));
    } finally {
      InstalledPluginServicesTestSupport.shutdown(installedPlugins);
      System.clearProperty("ircafe.test.bouncerDiscoveryMarker");
      System.clearProperty("ircafe.test.bouncerDisconnectMarker");
    }
  }

  private static String guideProviderSource() {
    return """
        package example.bouncer;

        import cafe.woden.ircclient.bouncer.spi.BouncerBackendDiscoveryHandler;
        import cafe.woden.ircclient.bouncer.spi.BouncerDiscoveredNetwork;
        import java.nio.file.Files;
        import java.nio.file.Path;

        public final class ExampleBouncerDiscoveryHandler
            implements BouncerBackendDiscoveryHandler {
          @Override
          public String backendId() {
            return " Guide-Bouncer ";
          }

          @Override
          public void onNetworkDiscovered(BouncerDiscoveredNetwork network) {
            String marker = System.getProperty("ircafe.test.bouncerDiscoveryMarker");
            if (marker == null || marker.isBlank()) {
              return;
            }
            try {
              Files.writeString(
                  Path.of(marker),
                  network.originServerId()
                      + "|"
                      + network.networkId()
                      + "|"
                      + network.displayName()
                      + "|"
                      + network.loginUserHint()
                      + "|"
                      + network.hasCapability("networks")
                      + "|"
                      + network.attributes().get("plan"));
            } catch (Exception ignored) {
            }
          }

          @Override
          public void onOriginDisconnected(String originServerId) {
            String marker = System.getProperty("ircafe.test.bouncerDisconnectMarker");
            if (marker == null || marker.isBlank()) {
              return;
            }
            try {
              Files.writeString(Path.of(marker), originServerId);
            } catch (Exception ignored) {
            }
          }
        }
        """;
  }
}
