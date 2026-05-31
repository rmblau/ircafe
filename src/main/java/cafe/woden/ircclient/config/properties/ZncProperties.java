package cafe.woden.ircclient.config.properties;

import cafe.woden.ircclient.config.ConfigPropertyKeys;
import java.util.Map;
import org.jmolecules.architecture.layered.InfrastructureLayer;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** ZNC bouncer integration settings. */
@ConfigurationProperties(prefix = ConfigPropertyKeys.IRCAFE_ZNC_PREFIX)
@InfrastructureLayer
public record ZncProperties(Map<String, Map<String, Boolean>> autoConnect, Discovery discovery) {

  public ZncProperties {
    if (autoConnect == null) autoConnect = Map.of();
    if (discovery == null) discovery = new Discovery(true);
  }

  /** ZNC network discovery settings. */
  public record Discovery(boolean enabled) {
    public Discovery {
      // Defaults are handled by the parent record ctor (enabled=true).
    }
  }

  public Map<String, Boolean> autoConnectForBouncer(String bouncerServerId) {
    return BouncerAutoConnectPropertiesSupport.autoConnectForBouncer(autoConnect, bouncerServerId);
  }

  public Map<String, Map<String, Boolean>> autoConnectCopy() {
    return BouncerAutoConnectPropertiesSupport.autoConnectCopy(autoConnect);
  }
}
