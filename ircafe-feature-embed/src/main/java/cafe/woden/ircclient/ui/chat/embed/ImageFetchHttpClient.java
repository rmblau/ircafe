package cafe.woden.ircclient.ui.chat.embed;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

/** Feature-safe HTTP port used by image download orchestration. */
public interface ImageFetchHttpClient {

  ImageFetchHttpResponse getStream(String serverId, URI uri, Map<String, String> requestHeaders)
      throws IOException, InterruptedException;
}
