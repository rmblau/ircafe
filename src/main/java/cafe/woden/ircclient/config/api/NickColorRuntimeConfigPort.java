package cafe.woden.ircclient.config.api;

import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.architecture.layered.ApplicationLayer;

/** Runtime-config contract for nickname coloring preferences. */
@SecondaryPort
@ApplicationLayer
public interface NickColorRuntimeConfigPort {

  void rememberNickColoringEnabled(boolean enabled);

  void rememberNickColorMinContrast(double minContrast);
}
