package cafe.woden.ircclient.ui.chat.embed;

import cafe.woden.ircclient.ui.chat.embed.spi.EmbedHttpHeaderProvider;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import org.jmolecules.architecture.layered.InterfaceLayer;

/**
 * Root-independent image download orchestration.
 *
 * <p>The root app supplies the HTTP adapter so proxy/SOCKS/transport choices stay root-owned. This
 * service owns URL validation, request header assembly, HTTP response decisions, body reading, and
 * retry recursion.
 */
@InterfaceLayer
public class ImageFetchDownloadService {

  public static final int DEFAULT_MAX_BYTES = 20 * 1024 * 1024;

  private final ImageFetchHttpClient httpClient;
  private final Collection<? extends EmbedHttpHeaderProvider> headerProviders;
  private final ImageFetchHttpHeaders headers;
  private final ImageFetchResponseReader responseReader;
  private final ImageFetchResponsePolicy responsePolicy;
  private final int maxBytes;
  private final Consumer<LinkPreviewHttpHeaderProviderFailure> headerFailureConsumer;

  public ImageFetchDownloadService(ImageFetchHttpClient httpClient) {
    this(httpClient, List.of());
  }

  public ImageFetchDownloadService(
      ImageFetchHttpClient httpClient,
      Collection<? extends EmbedHttpHeaderProvider> headerProviders) {
    this(
        httpClient,
        headerProviders,
        new ImageFetchHttpHeaders(),
        new ImageFetchResponseReader(),
        new ImageFetchResponsePolicy(),
        DEFAULT_MAX_BYTES,
        failure -> {});
  }

  public ImageFetchDownloadService(
      ImageFetchHttpClient httpClient,
      Collection<? extends EmbedHttpHeaderProvider> headerProviders,
      ImageFetchHttpHeaders headers,
      ImageFetchResponseReader responseReader,
      ImageFetchResponsePolicy responsePolicy,
      int maxBytes,
      Consumer<LinkPreviewHttpHeaderProviderFailure> headerFailureConsumer) {
    this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
    this.headerProviders = headerProviders == null ? List.of() : List.copyOf(headerProviders);
    this.headers = headers != null ? headers : new ImageFetchHttpHeaders();
    this.responseReader = responseReader != null ? responseReader : new ImageFetchResponseReader();
    this.responsePolicy = responsePolicy != null ? responsePolicy : new ImageFetchResponsePolicy();
    this.maxBytes = maxBytes > 0 ? maxBytes : DEFAULT_MAX_BYTES;
    this.headerFailureConsumer =
        headerFailureConsumer != null ? headerFailureConsumer : failure -> {};
  }

  public byte[] download(String serverId, String url) throws IOException, InterruptedException {
    return download(serverId, url, 0);
  }

  private byte[] download(String serverId, String url, int attempt)
      throws IOException, InterruptedException {
    URI uri = URI.create(url);
    String scheme = uri.getScheme();
    if (scheme == null) {
      throw new IOException("URL has no scheme: " + url);
    }
    scheme = scheme.toLowerCase(Locale.ROOT);
    if (!scheme.equals("http") && !scheme.equals("https")) {
      throw new IOException("Unsupported URL scheme for image embed: " + scheme);
    }

    LinkPreviewHttpHeaderResult headerResult = headers.headersFor(uri, headerProviders);
    for (LinkPreviewHttpHeaderProviderFailure failure : headerResult.failures()) {
      headerFailureConsumer.accept(failure);
    }
    Map<String, String> requestHeaders = headerResult.headers();

    ImageFetchHttpResponse response = httpClient.getStream(serverId, uri, requestHeaders);
    int code = response.statusCode();
    String contentType = response.firstHeader("content-type").orElse("");
    long contentLength = response.firstHeaderAsLong("content-length").orElse(-1L);
    ImageFetchResponseDecision responseDecision =
        responsePolicy.decide(code, contentLength, url, attempt, maxBytes);
    if (!responseDecision.readBodyRequested()) {
      try (InputStream ignored = response.body()) {
        // Ensure the root adapter can release the underlying connection.
      }
      if (responseDecision.retryRequested()) {
        return download(serverId, responseDecision.retryUrl().orElseThrow(), attempt + 1);
      }
      throw new IOException(responseDecision.message());
    }

    ImageFetchReadResult readResult =
        responseReader.read(response.body(), contentType, url, attempt, maxBytes);
    if (readResult.retryRequested()) {
      return download(serverId, readResult.retryUrl().orElseThrow(), attempt + 1);
    }
    return readResult.bytes();
  }
}
