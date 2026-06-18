package cafe.woden.ircclient.ui.chat.embed.spi;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;

/**
 * HTTP facade exposed to ServiceLoader-backed link preview resolvers.
 *
 * <p>The concrete application implementation applies runtime proxy settings and embed HTTP header
 * providers before fetching.
 */
public interface LinkPreviewHttp {

  LinkPreviewHttpResponse<InputStream> getStream(URI uri, String accept) throws IOException;

  LinkPreviewHttpResponse<InputStream> getStream(
      URI uri, String accept, Map<String, String> extraHeaders) throws IOException;

  LinkPreviewHttpResponse<String> getString(URI uri) throws IOException;

  LinkPreviewHttpResponse<String> getString(URI uri, Map<String, String> extraHeaders)
      throws IOException;

  LinkPreviewHttpResponse<String> getString(
      URI uri, String accept, Map<String, String> extraHeaders) throws IOException;
}
