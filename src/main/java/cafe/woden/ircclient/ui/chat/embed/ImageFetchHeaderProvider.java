package cafe.woden.ircclient.ui.chat.embed;

import java.net.URI;
import java.util.Map;
import org.jmolecules.architecture.layered.InterfaceLayer;

/**
 * Compatibility adapter for plugins that contributed image-fetch HTTP headers before the shared
 * embed header SPI existed.
 *
 * @deprecated implement and register {@link
 *     cafe.woden.ircclient.ui.chat.embed.spi.EmbedHttpHeaderProvider} instead. This adapter remains
 *     loadable so older plugin jars continue to work during the migration window.
 */
@InterfaceLayer
@Deprecated(forRemoval = false)
public interface ImageFetchHeaderProvider extends EmbedHttpHeaderProvider {

  /**
   * Returns extra HTTP headers to apply when fetching the given image URI.
   *
   * <p>Providers should return an empty map when they do not recognize the URI.
   */
  Map<String, String> imageFetchHeaders(URI imageUri);

  @Override
  default Map<String, String> embedHttpHeaders(URI uri) {
    return imageFetchHeaders(uri);
  }
}
