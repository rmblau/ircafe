package cafe.woden.ircclient.translation;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

/** App-provided HTTP seam for feature-owned translation backends. */
public interface MessageTranslationHttpClient {

  MessageTranslationHttpResponse getString(
      URI endpoint, Map<String, String> headers, long timeoutMs) throws IOException;

  MessageTranslationHttpResponse postString(
      URI endpoint, Map<String, String> headers, String body, long timeoutMs) throws IOException;
}
