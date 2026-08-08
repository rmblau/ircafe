package cafe.woden.ircclient.config.api;

import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.architecture.layered.ApplicationLayer;

/** Runtime-config contract for reading the default IRC QUIT message. */
@SecondaryPort
@ApplicationLayer
public interface QuitMessageRuntimeConfigPort {

  String DEFAULT_QUIT_MESSAGE = "Client shutdown: IRCafe https://github.com/wodencafe/ircafe";

  String readDefaultQuitMessage();
}
