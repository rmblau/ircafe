package cafe.woden.ircclient.config.api;

import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.architecture.layered.ApplicationLayer;

/** Runtime-config contract for the update notifier enabled state. */
@SecondaryPort
@ApplicationLayer
public interface UpdateNotifierRuntimeConfigPort {

  boolean readUpdateNotifierEnabled(boolean defaultValue);

  void rememberUpdateNotifierEnabled(boolean enabled);
}
