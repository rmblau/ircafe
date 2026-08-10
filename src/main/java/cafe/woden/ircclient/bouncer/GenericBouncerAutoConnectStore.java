package cafe.woden.ircclient.bouncer;

import cafe.woden.ircclient.config.api.BouncerDiscoveryConfigPort;
import java.util.Objects;
import org.jmolecules.architecture.layered.ApplicationLayer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Persisted auto-connect preferences for generic bouncer-discovered networks. */
@Component
@ApplicationLayer
public class GenericBouncerAutoConnectStore extends AbstractBouncerAutoConnectStore {

  private final BouncerAutoConnectNetworkKeyNormalizer networkKeyNormalizer;

  @Autowired
  public GenericBouncerAutoConnectStore(BouncerDiscoveryConfigPort runtimeConfig) {
    this(runtimeConfig, new BouncerAutoConnectNetworkKeyNormalizer());
  }

  GenericBouncerAutoConnectStore(
      BouncerDiscoveryConfigPort runtimeConfig,
      BouncerAutoConnectNetworkKeyNormalizer networkKeyNormalizer) {
    super(runtimeConfig);
    this.networkKeyNormalizer =
        Objects.requireNonNull(networkKeyNormalizer, "networkKeyNormalizer");
    initialize(runtimeConfig.readGenericBouncerAutoConnectRules());
  }

  @Override
  protected String normalizeNetworkKey(String networkName) {
    return networkKeyNormalizer.normalize(networkName);
  }

  @Override
  protected void persistAutoConnectRule(String bouncerServerId, String networkKey, boolean enable) {
    runtimeConfig().rememberGenericBouncerAutoConnectNetwork(bouncerServerId, networkKey, enable);
  }
}
