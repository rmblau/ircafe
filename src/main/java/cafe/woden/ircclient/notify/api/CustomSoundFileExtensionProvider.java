package cafe.woden.ircclient.notify.api;

import java.util.List;
import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.architecture.layered.ApplicationLayer;

/** ServiceLoader-backed contribution point for IRCafe custom sound file extensions. */
@SecondaryPort
@ApplicationLayer
public interface CustomSoundFileExtensionProvider {

  /**
   * Returns additional file extensions, without a leading dot, accepted by IRCafe custom sound
   * importers.
   */
  List<String> soundFileExtensions();
}
