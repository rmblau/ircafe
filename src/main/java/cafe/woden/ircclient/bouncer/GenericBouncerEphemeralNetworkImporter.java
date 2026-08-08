package cafe.woden.ircclient.bouncer;

import cafe.woden.ircclient.bouncer.spi.BouncerBackendDiscoveryHandler;
import cafe.woden.ircclient.bouncer.spi.BouncerDiscoveredNetwork;
import cafe.woden.ircclient.bouncer.spi.BouncerNetworkMappingStrategy;
import cafe.woden.ircclient.bouncer.spi.BuiltInBouncerBackendIds;
import cafe.woden.ircclient.config.api.BouncerDiscoveryConfigPort;
import cafe.woden.ircclient.config.servers.EphemeralServerRegistry;
import cafe.woden.ircclient.config.servers.ServerRegistry;
import java.util.Objects;
import org.jmolecules.architecture.layered.ApplicationLayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Generic backend handler for bouncer discovery events. */
@Component
@ApplicationLayer
public class GenericBouncerEphemeralNetworkImporter implements BouncerBackendDiscoveryHandler {

  private static final Logger log =
      LoggerFactory.getLogger(GenericBouncerEphemeralNetworkImporter.class);

  private final BouncerNetworkDiscoveryOrchestrator orchestrator;

  public GenericBouncerEphemeralNetworkImporter(
      ServerRegistry serverRegistry,
      EphemeralServerRegistry ephemeralServers,
      GenericBouncerAutoConnectStore autoConnect,
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

  GenericBouncerEphemeralNetworkImporter(
      BouncerNetworkMappingStrategy mappingStrategy,
      ServerRegistry serverRegistry,
      EphemeralServerRegistry ephemeralServers,
      GenericBouncerAutoConnectStore autoConnect,
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
        .mappingStrategyOrMissing(BuiltInBouncerBackendIds.GENERIC);
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
}
