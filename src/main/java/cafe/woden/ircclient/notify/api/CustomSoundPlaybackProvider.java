package cafe.woden.ircclient.notify.api;

import java.nio.file.Path;
import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.architecture.layered.ApplicationLayer;

/** ServiceLoader-backed contribution point for playing imported IRCafe custom sound files. */
@SecondaryPort
@ApplicationLayer
public interface CustomSoundPlaybackProvider {

  /**
   * Attempts to play the custom sound file at {@code path}.
   *
   * @return {@code true} when this provider handled playback, or {@code false} to let IRCafe try
   *     the next provider/default Java Sound playback.
   */
  boolean playCustomSound(Path path) throws Exception;
}
