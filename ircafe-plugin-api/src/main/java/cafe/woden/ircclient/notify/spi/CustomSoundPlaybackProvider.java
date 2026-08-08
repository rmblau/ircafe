package cafe.woden.ircclient.notify.spi;

import java.nio.file.Path;

/**
 * ServiceLoader-backed contribution point for playing imported IRCafe custom sound files.
 *
 * <p>Plugins register public, stateless implementations with public no-argument constructors in
 * {@code META-INF/services/cafe.woden.ircclient.notify.spi.CustomSoundPlaybackProvider}. IRCafe owns
 * executor scheduling, playback freshness/rate limiting, provider failure isolation, Java Sound
 * fallback, and the lifecycle of the supplied file.
 */
public interface CustomSoundPlaybackProvider {

  /**
   * Attempts to play the existing, app-resolved custom sound file at {@code path}.
   *
   * <p>Return {@code true} only after taking ownership of this playback request. Return {@code false}
   * for unsupported or deliberately unhandled files so IRCafe can try later providers and its Java
   * Sound fallback. Throwing an exception does not claim the request; IRCafe isolates the failure
   * and continues the chain. Providers must not move, delete, or retain ownership of the supplied
   * path.
   *
   * @param path existing custom sound file resolved by IRCafe
   * @return {@code true} when this provider handled playback, or {@code false} to continue the
   *     provider chain/default Java Sound playback
   * @throws Exception when playback fails; IRCafe isolates the failure and continues the chain
   */
  boolean playCustomSound(Path path) throws Exception;
}
