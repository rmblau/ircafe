package cafe.woden.ircclient.config.api;

import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.architecture.layered.ApplicationLayer;

/** Runtime-config contract for the lag indicator enabled state. */
@SecondaryPort
@ApplicationLayer
public interface LagIndicatorRuntimeConfigPort {

  boolean readLagIndicatorEnabled(boolean defaultValue);

  void rememberLagIndicatorEnabled(boolean enabled);
}
