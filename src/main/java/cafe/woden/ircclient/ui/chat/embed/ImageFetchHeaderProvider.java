package cafe.woden.ircclient.ui.chat.embed;

import java.net.URI;
import java.util.Map;
import org.jmolecules.architecture.layered.InterfaceLayer;

/**
 * ServiceLoader-backed contribution point for image fetch HTTP headers.
 *
 * @deprecated implement {@link EmbedHttpHeaderProvider} instead.
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
