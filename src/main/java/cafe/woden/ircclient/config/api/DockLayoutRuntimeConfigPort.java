package cafe.woden.ircclient.config.api;

import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.architecture.layered.ApplicationLayer;

/** Runtime-config contract for persisted shell dock layout state. */
@SecondaryPort
@ApplicationLayer
public interface DockLayoutRuntimeConfigPort extends RuntimeConfigPathPort {

  void rememberServerDockWidthPx(int serverDockWidthPx);

  void rememberUserDockWidthPx(int userDockWidthPx);

  void rememberPreserveDockLayout(boolean preserveDockLayout);
}
