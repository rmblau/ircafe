package cafe.woden.ircclient.translation;

import cafe.woden.ircclient.config.IrcProperties;
import cafe.woden.ircclient.net.HttpHeaderNames;
import cafe.woden.ircclient.net.HttpLite;
import cafe.woden.ircclient.net.NetProxyContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Objects;

abstract class AbstractHttpMessageTranslationBackend {

  static final String CONTENT_TYPE_JSON = "application/json";
  static final String USER_AGENT = "IRCafe";

  private static final ObjectMapper JSON = new ObjectMapper();

  HttpLite.Response<String> postJson(
      URI endpoint, Map<String, String> headers, String payload, long timeoutMs)
      throws IOException {
    return HttpLite.postString(
        endpoint,
        headers,
        payload,
        NetProxyContext.proxy(),
        timeoutMillis(timeoutMs),
        timeoutMillis(timeoutMs));
  }

  HttpLite.Response<String> getString(URI endpoint, Map<String, String> headers, long timeoutMs)
      throws IOException {
    return HttpLite.getString(
        endpoint,
        headers,
        NetProxyContext.proxy(),
        timeoutMillis(timeoutMs),
        timeoutMillis(timeoutMs));
  }

  String jsonFor(Object payload) throws IOException {
    return JSON.writeValueAsString(payload);
  }

  JsonNode json(String body) throws IOException {
    return JSON.readTree(Objects.toString(body, "{}"));
  }

  static URI endpointUri(IrcProperties.Client.Translation settings) {
    String endpoint = settings != null ? settings.endpoint() : "";
    endpoint = Objects.toString(endpoint, "").trim();
    if (endpoint.isBlank()) {
      throw new IllegalArgumentException("Translation endpoint is required.");
    }
    return URI.create(endpoint);
  }

  static void require2xx(HttpLite.Response<String> response, String provider) throws IOException {
    int status = response != null ? response.statusCode() : 0;
    if (status >= 200 && status < 300) {
      return;
    }
    String body = response != null ? Objects.toString(response.body(), "").trim() : "";
    if (body.length() > 240) {
      body = body.substring(0, 240) + "...";
    }
    throw new IOException(provider + " translation request failed (" + status + "): " + body);
  }

  static Map<String, String> jsonHeaders() {
    return Map.of(
        HttpHeaderNames.CONTENT_TYPE, CONTENT_TYPE_JSON,
        HttpHeaderNames.ACCEPT, CONTENT_TYPE_JSON,
        HttpHeaderNames.USER_AGENT, USER_AGENT);
  }

  static int timeoutMillis(long timeoutMs) {
    long normalized = timeoutMs <= 0 ? 10_000L : Math.min(timeoutMs, 120_000L);
    return Math.toIntExact(normalized);
  }

  static String lower(String value) {
    return Objects.toString(value, "").trim().toLowerCase(java.util.Locale.ROOT);
  }

  static String upper(String value) {
    return Objects.toString(value, "").trim().toUpperCase(java.util.Locale.ROOT);
  }
}
