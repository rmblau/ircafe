package cafe.woden.ircclient.config.api;

import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.architecture.layered.ApplicationLayer;

/** Runtime-config contract for persisting the preferred nick of a configured server. */
@SecondaryPort
@ApplicationLayer
public interface PreferredNickRuntimeConfigPort {

  void rememberNick(String serverId, String nick);
}
