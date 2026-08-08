package cafe.woden.ircclient.irc.soju;

import cafe.woden.ircclient.bouncer.AbstractBouncerAutoConnectStore;
import cafe.woden.ircclient.bouncer.BouncerAutoConnectNetworkKeyNormalizer;
import cafe.woden.ircclient.config.api.BouncerDiscoveryConfigPort;
import cafe.woden.ircclient.config.properties.SojuProperties;
import java.util.Map;
import java.util.Objects;
import org.jmolecules.architecture.layered.ApplicationLayer;
import org.springframework.stereotype.Component;

/**
 * Persisted auto-connect preferences for Soju-discovered networks.
 *
 * <p>This store keeps a mapping of:
 *
 * <pre>
 *   bouncerServerId -> networkName -> enabled
 * </pre>
 *
 * <p>Network names are canonicalized using the feature-owned bouncer key normalizer and compared
 * case-insensitively.
 */
@Component
@ApplicationLayer
public class SojuAutoConnectStore extends AbstractBouncerAutoConnectStore {

  private final BouncerAutoConnectNetworkKeyNormalizer networkKeyNormalizer;

  public SojuAutoConnectStore(SojuProperties props, BouncerDiscoveryConfigPort runtimeConfig) {
    this(props, runtimeConfig, new BouncerAutoConnectNetworkKeyNormalizer());
  }

  SojuAutoConnectStore(
      SojuProperties props,
      BouncerDiscoveryConfigPort runtimeConfig,
      BouncerAutoConnectNetworkKeyNormalizer networkKeyNormalizer) {
    super(runtimeConfig);
    this.networkKeyNormalizer =
        Objects.requireNonNull(networkKeyNormalizer, "networkKeyNormalizer");
    initialize(props == null ? Map.of() : props.autoConnectCopy());
  }

  /** Convenience alias. */
  public synchronized Map<String, Boolean> rulesForBouncer(String bouncerServerId) {
    return networksForBouncer(bouncerServerId);
  }

  @Override
  protected String normalizeNetworkKey(String networkName) {
    return networkKeyNormalizer.normalize(networkName);
  }

  @Override
  protected void persistAutoConnectRule(String bouncerServerId, String networkKey, boolean enable) {
    runtimeConfig().rememberSojuAutoConnectNetwork(bouncerServerId, networkKey, enable);
  }
}
