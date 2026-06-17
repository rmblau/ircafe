package cafe.woden.ircclient.ui.chat.embed.spi;

import cafe.woden.ircclient.net.HttpLite;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import org.jmolecules.architecture.layered.InterfaceLayer;

/**
 * HTTP facade exposed to ServiceLoader-backed link preview resolvers.
 *
 * <p>The concrete application implementation applies runtime proxy settings and embed HTTP header
 * providers before fetching.
 */
@InterfaceLayer
public interface LinkPreviewHttp {

  HttpLite.Response<InputStream> getStream(URI uri, String accept) throws IOException;

  HttpLite.Response<InputStream> getStream(URI uri, String accept, Map<String, String> extraHeaders)
      throws IOException;

  HttpLite.Response<String> getString(URI uri) throws IOException;

  HttpLite.Response<String> getString(URI uri, Map<String, String> extraHeaders) throws IOException;

  HttpLite.Response<String> getString(URI uri, String accept, Map<String, String> extraHeaders)
      throws IOException;
}
