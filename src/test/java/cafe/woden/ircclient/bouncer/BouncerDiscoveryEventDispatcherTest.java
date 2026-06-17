package cafe.woden.ircclient.bouncer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import cafe.woden.ircclient.bouncer.spi.BouncerBackendDiscoveryHandler;
import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.junit.jupiter.api.Test;

class BouncerDiscoveryEventDispatcherTest {

  @Test
  void routesDiscoveryEventsToPluginHandlersFromInstalledPluginsPort() {
    RecordingPluginHandler pluginHandler = new RecordingPluginHandler("plugin-bouncer");
    BouncerDiscoveryEventDispatcher dispatcher =
        new BouncerDiscoveryEventDispatcher(
            List.of(new RecordingHandler("generic")),
            new FakeInstalledPluginsPort(List.of(pluginHandler)));

    dispatcher.onNetworkDiscovered(
        new BouncerDiscoveredNetwork(
            "PLUGIN-BOUNCER", "origin", "network", "Network", "Network", Map.of()));
    dispatcher.onOriginDisconnected("plugin-bouncer", "origin");

    assertEquals(List.of("network"), pluginHandler.discoveredNetworkIds);
    assertEquals(List.of("origin"), pluginHandler.disconnectedOriginServerIds);
  }

  @Test
  void routesDiscoveryEventsToPluginHandlerSpiFromInstalledPluginsPort() {
    RecordingSpiHandler pluginHandler = new RecordingSpiHandler("plugin-spi");
    BouncerDiscoveryEventDispatcher dispatcher =
        new BouncerDiscoveryEventDispatcher(
            List.of(new RecordingHandler("generic")),
            new FakeInstalledPluginsPort(List.of(pluginHandler)));

    dispatcher.onNetworkDiscovered(
        new BouncerDiscoveredNetwork(
            "PLUGIN-SPI", "origin", "network", "Network", "Network", Map.of()));
    dispatcher.onOriginDisconnected("plugin-spi", "origin");

    assertEquals(List.of("network"), pluginHandler.discoveredNetworkIds);
    assertEquals(List.of("origin"), pluginHandler.disconnectedOriginServerIds);
  }

  private static final class RecordingHandler implements BouncerBackendDiscoveryHandler {
    private final String backendId;
    private final List<String> discoveredNetworkIds = new ArrayList<>();
    private final List<String> disconnectedOriginServerIds = new ArrayList<>();

    private RecordingHandler(String backendId) {
      this.backendId = backendId;
    }

    @Override
    public String backendId() {
      return backendId;
    }

    @Override
    public void onNetworkDiscovered(BouncerDiscoveredNetwork network) {
      discoveredNetworkIds.add(network.networkId());
    }

    @Override
    public void onOriginDisconnected(String originServerId) {
      disconnectedOriginServerIds.add(originServerId);
    }
  }

  private static final class RecordingPluginHandler implements BouncerBackendDiscoveryHandler {
    private final String backendId;
    private final List<String> discoveredNetworkIds = new ArrayList<>();
    private final List<String> disconnectedOriginServerIds = new ArrayList<>();

    private RecordingPluginHandler(String backendId) {
      this.backendId = backendId;
    }

    @Override
    public String backendId() {
      return backendId;
    }

    @Override
    public void onNetworkDiscovered(BouncerDiscoveredNetwork network) {
      discoveredNetworkIds.add(network.networkId());
    }

    @Override
    public void onOriginDisconnected(String originServerId) {
      disconnectedOriginServerIds.add(originServerId);
    }
  }

  private static final class RecordingSpiHandler
      implements cafe.woden.ircclient.bouncer.spi.BouncerBackendDiscoveryHandler {
    private final String backendId;
    private final List<String> discoveredNetworkIds = new ArrayList<>();
    private final List<String> disconnectedOriginServerIds = new ArrayList<>();

    private RecordingSpiHandler(String backendId) {
      this.backendId = backendId;
    }

    @Override
    public String backendId() {
      return backendId;
    }

    @Override
    public void onNetworkDiscovered(BouncerDiscoveredNetwork network) {
      discoveredNetworkIds.add(network.networkId());
    }

    @Override
    public void onOriginDisconnected(String originServerId) {
      disconnectedOriginServerIds.add(originServerId);
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
