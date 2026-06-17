package cafe.woden.ircclient.config.api;

import java.util.Map;
import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.architecture.layered.ApplicationLayer;

/** Runtime-config contract for preferences-managed IRCv3 capability toggles. */
@SecondaryPort
@ApplicationLayer
public interface Ircv3CapabilityConfigPort {

  Map<String, Boolean> readIrcv3Capabilities();

  void rememberIrcv3CapabilityEnabled(String capability, boolean enabled);
}
