package cafe.woden.ircclient.irc.soju;

import cafe.woden.ircclient.bouncer.BouncerBackendRegistry;
import cafe.woden.ircclient.bouncer.BouncerConnectionPort;
import cafe.woden.ircclient.bouncer.BouncerNetworkDiscoveryOrchestrator;
import cafe.woden.ircclient.bouncer.spi.BouncerBackendDiscoveryHandler;
import cafe.woden.ircclient.bouncer.spi.BouncerDiscoveredNetwork;
import cafe.woden.ircclient.bouncer.spi.BouncerNetworkMappingStrategy;
import cafe.woden.ircclient.bouncer.spi.BouncerServerProfile;
import cafe.woden.ircclient.bouncer.spi.BuiltInBouncerBackendIds;
import cafe.woden.ircclient.bouncer.spi.ResolvedBouncerNetwork;
import cafe.woden.ircclient.config.api.BouncerDiscoveryConfigPort;
import cafe.woden.ircclient.config.servers.EphemeralServerRegistry;
import cafe.woden.ircclient.config.servers.ServerRegistry;
import java.util.Objects;
import org.jmolecules.architecture.layered.ApplicationLayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Creates/updates ephemeral server entries for Soju-discovered networks.
 *
 * <p>These servers are not persisted to the runtime YAML; they exist only for the duration of the
 * bouncer control session.
 */
@Component
@ApplicationLayer
public class SojuEphemeralNetworkImporter implements BouncerBackendDiscoveryHandler {

  private static final Logger log = LoggerFactory.getLogger(SojuEphemeralNetworkImporter.class);

  private final BouncerNetworkDiscoveryOrchestrator orchestrator;
  private final SojuBouncerDiscoveryAdapter discoveryAdapter = new SojuBouncerDiscoveryAdapter();

  public SojuEphemeralNetworkImporter(
      ServerRegistry serverRegistry,
      EphemeralServerRegistry ephemeralServers,
      SojuAutoConnectStore autoConnect,
      BouncerDiscoveryConfigPort runtimeConfig,
      BouncerConnectionPort connectionPort,
      BouncerBackendRegistry bouncerBackends) {
    this(
        mappingStrategy(bouncerBackends),
        serverRegistry,
        ephemeralServers,
        autoConnect,
        runtimeConfig,
        connectionPort);
  }

  SojuEphemeralNetworkImporter(
      BouncerNetworkMappingStrategy mappingStrategy,
      ServerRegistry serverRegistry,
      EphemeralServerRegistry ephemeralServers,
      SojuAutoConnectStore autoConnect,
      BouncerDiscoveryConfigPort runtimeConfig,
      BouncerConnectionPort connectionPort) {
    this.orchestrator =
        new BouncerNetworkDiscoveryOrchestrator(
            log,
            Objects.requireNonNull(mappingStrategy, "mappingStrategy"),
            serverRegistry,
            ephemeralServers,
            autoConnect,
            runtimeConfig,
            connectionPort);
  }

  private static BouncerNetworkMappingStrategy mappingStrategy(
      BouncerBackendRegistry bouncerBackends) {
    return Objects.requireNonNull(bouncerBackends, "bouncerBackends")
        .mappingStrategy(BuiltInBouncerBackendIds.SOJU)
        .orElseGet(() -> missingMappingStrategy(BuiltInBouncerBackendIds.SOJU));
  }

  private static BouncerNetworkMappingStrategy missingMappingStrategy(String backendId) {
    return new BouncerNetworkMappingStrategy() {
      @Override
      public String backendId() {
        return backendId;
      }

      @Override
      public ResolvedBouncerNetwork resolveNetwork(
          BouncerServerProfile bouncer, BouncerDiscoveredNetwork network) {
        throw new IllegalStateException("Missing bouncer mapping strategy: " + backendId);
      }
    };
  }

  @Override
  public String backendId() {
    return orchestrator.backendId();
  }

  @Override
  public void onNetworkDiscovered(BouncerDiscoveredNetwork network) {
    orchestrator.onNetworkDiscovered(network);
  }

  @Override
  public void onOriginDisconnected(String originServerId) {
    orchestrator.onOriginDisconnected(originServerId);
  }

  public void onSojuNetworkDiscovered(SojuNetwork network) {
    BouncerDiscoveredNetwork discovered = discoveryAdapter.fromSojuNetwork(network);
    onNetworkDiscovered(discovered);
  }

  public void onSojuOriginDisconnected(String originServerId) {
    onOriginDisconnected(originServerId);
  }
}
