package cafe.woden.ircclient.config.api;

import cafe.woden.ircclient.config.IrcProperties;
import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.architecture.layered.ApplicationLayer;

/** Runtime-config contract for persisted client translation settings. */
@SecondaryPort
@ApplicationLayer
public interface ClientTranslationRuntimeConfigPort {

  void rememberClientTranslation(IrcProperties.Client.Translation translation);
}
