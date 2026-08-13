package cafe.woden.ircclient.notify.spi;

import java.util.List;

/**
 * ServiceLoader-backed contribution point for IRCafe custom sound file extensions.
 *
 * <p>Plugins register public, stateless implementations with public no-argument constructors in
 * {@code META-INF/services/cafe.woden.ircclient.notify.spi.CustomSoundFileExtensionProvider}.
 * Extension contributions affect file-name acceptance and chooser/import metadata only; IRCafe
 * retains file copying, runtime-path, settings, and playback ownership.
 */
public interface CustomSoundFileExtensionProvider {

  /**
   * Returns additional accepted file extensions.
   *
   * <p>Providers should normally return lower-case values without a leading dot. IRCafe trims each
   * value, removes leading dots, compares values case-insensitively, de-duplicates them, and
   * ignores blank or non-alphanumeric entries. Returning an extension does not imply that Java
   * Sound can decode it; pair this contribution with {@link CustomSoundPlaybackProvider} when
   * custom playback is required.
   *
   * @return contributed file extensions; invalid entries are ignored by IRCafe
   */
  List<String> soundFileExtensions();
}
