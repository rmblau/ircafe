package cafe.woden.ircclient.irc.znc;

import cafe.woden.ircclient.bouncer.AbstractBouncerAutoConnectStore;
import cafe.woden.ircclient.bouncer.ZncAutoConnectNetworkKeyNormalizer;
import cafe.woden.ircclient.config.api.BouncerDiscoveryConfigPort;
import cafe.woden.ircclient.config.properties.ZncProperties;
import java.util.Map;
import java.util.Objects;
import org.jmolecules.architecture.layered.ApplicationLayer;
import org.springframework.stereotype.Component;

/**
 * Persisted auto-connect preferences for ZNC-discovered networks.
 *
 * <p>This store keeps a mapping of:
 *
 * <pre>
 *   bouncerServerId -> networkName -> enabled
 * </pre>
 *
 * <p>Network names are canonicalized using ZNC's network segment sanitizer and compared
 * case-insensitively.
 */
@Component
@ApplicationLayer
public class ZncAutoConnectStore extends AbstractBouncerAutoConnectStore {

  private final ZncAutoConnectNetworkKeyNormalizer networkKeyNormalizer;

  public ZncAutoConnectStore(ZncProperties props, BouncerDiscoveryConfigPort runtimeConfig) {
    this(props, runtimeConfig, new ZncAutoConnectNetworkKeyNormalizer());
  }

  ZncAutoConnectStore(
      ZncProperties props,
      BouncerDiscoveryConfigPort runtimeConfig,
      ZncAutoConnectNetworkKeyNormalizer networkKeyNormalizer) {
    super(runtimeConfig);
    this.networkKeyNormalizer =
        Objects.requireNonNull(networkKeyNormalizer, "networkKeyNormalizer");
    initialize(props == null ? Map.of() : props.autoConnectCopy());
  }

  @Override
  protected String normalizeNetworkKey(String networkName) {
    return networkKeyNormalizer.normalize(networkName);
  }

  @Override
  protected void persistAutoConnectRule(String bouncerServerId, String networkKey, boolean enable) {
    runtimeConfig().rememberZncAutoConnectNetwork(bouncerServerId, networkKey, enable);
  }
}
