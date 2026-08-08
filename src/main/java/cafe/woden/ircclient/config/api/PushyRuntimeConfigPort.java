package cafe.woden.ircclient.config.api;

import cafe.woden.ircclient.config.properties.PushyProperties;
import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.architecture.layered.ApplicationLayer;

/** Runtime-config contract for persisted Pushy notification settings. */
@SecondaryPort
@ApplicationLayer
public interface PushyRuntimeConfigPort {

  void rememberPushySettings(PushyProperties settings);
}
