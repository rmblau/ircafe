package cafe.woden.ircclient.ui.chat.embed;

import java.net.URI;
import java.util.Map;
import org.jmolecules.architecture.layered.InterfaceLayer;

/**
 * ServiceLoader-backed contribution point for link preview HTTP request headers.
 *
 * @deprecated implement {@link EmbedHttpHeaderProvider} instead.
 */
@InterfaceLayer
@Deprecated(forRemoval = false)
public interface PreviewHttpHeaderProvider extends EmbedHttpHeaderProvider {

  /**
   * Returns extra HTTP headers to apply when fetching the given link-preview URI.
   *
   * <p>Providers should return an empty map when they do not apply to the URI.
   */
  Map<String, String> previewHttpHeaders(URI uri);

  @Override
  default Map<String, String> embedHttpHeaders(URI uri) {
    return previewHttpHeaders(uri);
  }
}
