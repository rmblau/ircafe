package cafe.woden.ircclient.ui.chat.embed;

import cafe.woden.ircclient.net.HttpHeaderNames;
import cafe.woden.ircclient.net.HttpLite;
import cafe.woden.ircclient.net.ProxyPlan;
import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreviewHttp;
import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreviewHttpHeaders;
import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreviewHttpResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jmolecules.architecture.layered.InterfaceLayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HTTP helper used by link preview resolvers.
 *
 * <p>Uses {@link java.net.HttpURLConnection} via {@link HttpLite} so that SOCKS proxies can be
 * applied (the JDK {@code java.net.http.HttpClient} does not support SOCKS).
 */
@InterfaceLayer
public final class PreviewHttp implements LinkPreviewHttp {

  private static final Logger log = LoggerFactory.getLogger(PreviewHttp.class);

  // Package-visible so other embed helpers (e.g., ImageFetchService) can share the same headers.
  public static final String USER_AGENT = "ircafe-link-preview/1.0";
  // Some sites (notably IMDb) increasingly block non-browser user agents.
  // Use this when we need a browser-ish UA to avoid being served interstitial pages.
  public static final String BROWSER_USER_AGENT =
      "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36";
  public static final String ACCEPT_LANGUAGE = "en-US,en;q=0.9";
  public static final String HEADER_ACCEPT = HttpHeaderNames.ACCEPT;
  public static final String HEADER_ACCEPT_ENCODING = HttpHeaderNames.ACCEPT_ENCODING;
  public static final String HEADER_ACCEPT_LANGUAGE = HttpHeaderNames.ACCEPT_LANGUAGE;
  public static final String HEADER_REFERER = HttpHeaderNames.REFERER;
  public static final String HEADER_USER_AGENT = HttpHeaderNames.USER_AGENT;
  public static final String HEADER_X_GITHUB_API_VERSION = HttpHeaderNames.X_GITHUB_API_VERSION;
  private static final LinkPreviewHttpAdapterHeaders ADAPTER_HEADERS =
      new LinkPreviewHttpAdapterHeaders();

  private final Proxy proxy;
  private final int connectTimeoutMs;
  private final int readTimeoutMs;
  private final List<cafe.woden.ircclient.ui.chat.embed.spi.EmbedHttpHeaderProvider>
      headerProviders;

  public PreviewHttp(ProxyPlan plan) {
    this(plan, List.of());
  }

  public PreviewHttp(
      ProxyPlan plan,
      List<? extends cafe.woden.ircclient.ui.chat.embed.spi.EmbedHttpHeaderProvider>
          headerProviders) {
    ProxyPlan p = plan != null ? plan : ProxyPlan.direct();
    this.proxy = (p.proxy() != null) ? p.proxy() : Proxy.NO_PROXY;
    this.connectTimeoutMs = Math.max(1, p.connectTimeoutMs());
    this.readTimeoutMs = Math.max(1, p.readTimeoutMs());
    this.headerProviders = List.copyOf(headerProviders == null ? List.of() : headerProviders);
  }

  public LinkPreviewHttpResponse<InputStream> getStream(URI uri, String accept) throws IOException {
    return getStream(uri, accept, Map.of());
  }

  public LinkPreviewHttpResponse<InputStream> getStream(
      URI uri, String accept, Map<String, String> extraHeaders) throws IOException {
    Map<String, String> headers = headersFor(uri, accept, extraHeaders, headerProviders);

    return toLinkPreviewResponse(
        HttpLite.getStream(uri, headers, proxy, connectTimeoutMs, readTimeoutMs));
  }

  public LinkPreviewHttpResponse<String> getString(URI uri) throws IOException {
    return getString(uri, Map.of());
  }

  public LinkPreviewHttpResponse<String> getString(URI uri, Map<String, String> extraHeaders)
      throws IOException {
    return getString(
        uri, "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8", extraHeaders);
  }

  /**
   * Legacy-compatible overload used by many preview resolvers.
   *
   * @param uri target URI
   * @param accept explicit Accept header (e.g. application/json)
   * @param extraHeaders optional extra headers (may be null)
   */
  public LinkPreviewHttpResponse<String> getString(
      URI uri, String accept, Map<String, String> extraHeaders) throws IOException {
    Map<String, String> headers = headersFor(uri, accept, extraHeaders, headerProviders);

    return toLinkPreviewResponse(
        HttpLite.getString(uri, headers, proxy, connectTimeoutMs, readTimeoutMs));
  }

  static Map<String, String> headersFor(
      URI uri,
      String accept,
      Map<String, String> extraHeaders,
      List<? extends cafe.woden.ircclient.ui.chat.embed.spi.EmbedHttpHeaderProvider>
          headerProviders) {
    LinkPreviewHttpHeaderResult result =
        ADAPTER_HEADERS.headersFor(uri, accept, extraHeaders, headerProviders);
    for (LinkPreviewHttpHeaderProviderFailure failure : result.failures()) {
      cafe.woden.ircclient.ui.chat.embed.spi.EmbedHttpHeaderProvider provider = failure.provider();
      log.warn(
          "Preview HTTP header provider failed: {}",
          provider.getClass().getName(),
          failure.error());
    }
    return result.headers();
  }

  public static Optional<String> header(LinkPreviewHttpResponse<?> response, String name) {
    return response.headers().firstValue(name);
  }

  private static <T> LinkPreviewHttpResponse<T> toLinkPreviewResponse(
      HttpLite.Response<T> response) {
    return new LinkPreviewHttpResponse<>(
        response.statusCode(),
        new LinkPreviewHttpHeaders(response.headers().raw()),
        response.body());
  }

  public static Map<String, String> headers(Object... keyValues) {
    Map<String, String> m = new HashMap<>();
    for (int i = 0; i + 1 < keyValues.length; i += 2) {
      Object k = keyValues[i];
      Object v = keyValues[i + 1];
      if (k instanceof String ks && v instanceof String vs) {
        m.put(ks, vs);
      }
    }
    return m;
  }

  public static boolean looksLikeHtml(String contentType) {
    if (contentType == null) return false;
    String ct = contentType.toLowerCase();
    return ct.contains("text/html") || ct.contains("application/xhtml+xml");
  }

  public static String readUpTo(InputStream in, int maxBytes) throws IOException {
    try (in) {
      ByteArrayOutputStream out = new ByteArrayOutputStream(Math.min(maxBytes, 16 * 1024));
      byte[] buf = new byte[8 * 1024];
      int remaining = maxBytes;
      while (remaining > 0) {
        int read = in.read(buf, 0, Math.min(buf.length, remaining));
        if (read < 0) break;
        out.write(buf, 0, read);
        remaining -= read;
      }
      return out.toString(StandardCharsets.UTF_8);
    }
  }

  /**
   * Read up to {@code maxBytes} bytes from a stream, returning raw bytes.
   *
   * <p>This is used by HTML-based resolvers that want to hand a byte-limited body to jsoup. We
   * intentionally swallow IO failures and return an empty array to keep resolvers robust.
   */
  public static byte[] readUpToBytes(InputStream in, int maxBytes) {
    if (in == null || maxBytes <= 0) return new byte[0];
    try (in) {
      ByteArrayOutputStream out = new ByteArrayOutputStream(Math.min(maxBytes, 16 * 1024));
      byte[] buf = new byte[8 * 1024];
      int remaining = maxBytes;
      while (remaining > 0) {
        int n = in.read(buf, 0, Math.min(buf.length, remaining));
        if (n < 0) break;
        out.write(buf, 0, n);
        remaining -= n;
      }
      return out.toByteArray();
    } catch (IOException e) {
      return new byte[0];
    }
  }

  /**
   * Read up to {@code maxBytes} bytes from a previously fetched String body. This keeps older
   * resolvers that expect a byte-limited body working.
   */
  public static byte[] readUpToBytes(String body, int maxBytes) {
    if (body == null || maxBytes <= 0) return new byte[0];
    byte[] all = body.getBytes(StandardCharsets.UTF_8);
    if (all.length <= maxBytes) return all;
    byte[] out = new byte[maxBytes];
    System.arraycopy(all, 0, out, 0, maxBytes);
    return out;
  }
}
