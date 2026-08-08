package cafe.woden.ircclient.translation;

import cafe.woden.ircclient.net.HttpLite;
import cafe.woden.ircclient.net.NetProxyContext;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import org.jmolecules.architecture.layered.InfrastructureLayer;
import org.springframework.stereotype.Component;

/** Root adapter from feature-owned translation HTTP calls to app-owned HTTP/proxy policy. */
@Component
@InfrastructureLayer
public final class DefaultMessageTranslationHttpClient implements MessageTranslationHttpClient {

  @Override
  public MessageTranslationHttpResponse getString(
      URI endpoint, Map<String, String> headers, long timeoutMs) throws IOException {
    HttpLite.Response<String> response =
        HttpLite.getString(
            endpoint,
            headers,
            NetProxyContext.proxy(),
            timeoutMillis(timeoutMs),
            timeoutMillis(timeoutMs));
    return new MessageTranslationHttpResponse(response.statusCode(), response.body());
  }

  @Override
  public MessageTranslationHttpResponse postString(
      URI endpoint, Map<String, String> headers, String body, long timeoutMs) throws IOException {
    HttpLite.Response<String> response =
        HttpLite.postString(
            endpoint,
            headers,
            body,
            NetProxyContext.proxy(),
            timeoutMillis(timeoutMs),
            timeoutMillis(timeoutMs));
    return new MessageTranslationHttpResponse(response.statusCode(), response.body());
  }

  private static int timeoutMillis(long timeoutMs) {
    long normalized = timeoutMs <= 0 ? 10_000L : Math.min(timeoutMs, 120_000L);
    return Math.toIntExact(normalized);
  }
}
