package cafe.woden.ircclient.ui.chat.embed;

import java.util.HashMap;
import java.util.Map;

/** Root-independent HTTP defaults shared by feature-owned preview resolvers. */
final class LinkPreviewHttpDefaults {

  static final String USER_AGENT = "ircafe-link-preview/1.0";
  static final String BROWSER_USER_AGENT =
      "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36";
  static final String ACCEPT_LANGUAGE = "en-US,en;q=0.9";
  static final String HEADER_ACCEPT = "Accept";
  static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
  static final String HEADER_ACCEPT_LANGUAGE = "Accept-Language";
  static final String HEADER_REFERER = "Referer";
  static final String HEADER_USER_AGENT = "User-Agent";

  private LinkPreviewHttpDefaults() {}

  static Map<String, String> headers(Object... keyValues) {
    Map<String, String> headers = new HashMap<>();
    for (int i = 0; i + 1 < keyValues.length; i += 2) {
      Object key = keyValues[i];
      Object value = keyValues[i + 1];
      if (key instanceof String name && value instanceof String headerValue) {
        headers.put(name, headerValue);
      }
    }
    return Map.copyOf(headers);
  }
}
