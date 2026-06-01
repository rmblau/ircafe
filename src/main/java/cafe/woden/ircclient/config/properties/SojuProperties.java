package cafe.woden.ircclient.config.properties;

import java.util.Map;
import org.jmolecules.architecture.layered.InfrastructureLayer;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Soju bouncer integration settings. */
@ConfigurationProperties(prefix = ConfigPropertyKeys.IRCAFE_SOJU_PREFIX)
@InfrastructureLayer
public record SojuProperties(Map<String, Map<String, Boolean>> autoConnect, Discovery discovery) {

  public SojuProperties {
    if (autoConnect == null) autoConnect = Map.of();
    if (discovery == null) discovery = new Discovery(true);
  }

  /** Soju discovery settings. */
  public record Discovery(boolean enabled) {
    public Discovery {
      // Keep defaults stable even when config sections are partially present.
      // If the user omits this value entirely, we default to true.
    }
  }

  public Map<String, Boolean> autoConnectForBouncer(String bouncerServerId) {
    return BouncerAutoConnectPropertiesSupport.autoConnectForBouncer(autoConnect, bouncerServerId);
  }

  public Map<String, Map<String, Boolean>> autoConnectCopy() {
    return BouncerAutoConnectPropertiesSupport.autoConnectCopy(autoConnect);
  }
}
