package cafe.woden.ircclient.notify.spi;

import java.util.List;

/**
 * ServiceLoader-backed contribution point for IRCafe custom sound file extensions.
 *
 * <p>Plugins register implementations in {@code
 * META-INF/services/cafe.woden.ircclient.notify.spi.CustomSoundFileExtensionProvider}.
 */
public interface CustomSoundFileExtensionProvider {

  /**
   * Returns additional file extensions, without a leading dot, accepted by IRCafe custom sound
   * importers.
   */
  List<String> soundFileExtensions();
}
