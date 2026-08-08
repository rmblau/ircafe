package cafe.woden.ircclient.config.api;

import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.architecture.layered.ApplicationLayer;

/** Runtime-config contract for the one-time close-to-tray hint state. */
@SecondaryPort
@ApplicationLayer
public interface TrayCloseHintRuntimeConfigPort {

  boolean readTrayCloseToTrayHintShown(boolean defaultValue);

  void rememberTrayCloseToTrayHintShown(boolean shown);
}
