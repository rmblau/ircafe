package cafe.woden.ircclient.ui.chat.embed.spi;

import java.util.List;

/**
 * ServiceLoader contribution for direct-image URL extensions recognized by chat embeds.
 *
 * <p>Values may include or omit the leading dot. IRCafe trims and lowercases values, adds a missing
 * dot, rejects path-like values, and deduplicates extensions in provider order. Provider failures,
 * null lists, and null entries are ignored. The resulting extension set is used by direct-image
 * detection, link-preview exclusion, and image-viewer temporary-file naming.
 *
 * <p>Register implementations in {@code
 * META-INF/services/cafe.woden.ircclient.ui.chat.embed.spi.ImageUrlExtensionProvider}.
 * Implementations must be public and expose a public no-argument constructor.
 */
public interface ImageUrlExtensionProvider {

  /**
   * Returns additional direct-image file extensions.
   *
   * @return contributed extensions, or an empty list
   */
  List<String> imageFileExtensions();
}
