package cafe.woden.ircclient.irc.matrix;

import cafe.woden.ircclient.net.HttpHeaderNames;
import java.util.HashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** Shared Matrix HTTP header names and default values. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class MatrixHttpHeaders {

  static final String HEADER_CONTENT_TYPE = HttpHeaderNames.CONTENT_TYPE;

  private static final String APPLICATION_JSON = "application/json";
  private static final String GZIP = "gzip";
  private static final String BEARER_PREFIX = "Bearer ";

  static Map<String, String> json(String userAgent) {
    return Map.of(
        HttpHeaderNames.USER_AGENT, userAgent,
        HttpHeaderNames.ACCEPT, APPLICATION_JSON,
        HttpHeaderNames.ACCEPT_ENCODING, GZIP);
  }

  static Map<String, String> jsonWithContentType(String userAgent) {
    return Map.of(
        HttpHeaderNames.USER_AGENT,
        userAgent,
        HttpHeaderNames.ACCEPT,
        APPLICATION_JSON,
        HttpHeaderNames.ACCEPT_ENCODING,
        GZIP,
        HEADER_CONTENT_TYPE,
        APPLICATION_JSON);
  }

  static Map<String, String> withBearerToken(Map<String, String> baseHeaders, String token) {
    Map<String, String> headers = new HashMap<>(baseHeaders);
    headers.put(HttpHeaderNames.AUTHORIZATION, BEARER_PREFIX + token);
    return headers;
  }
}
