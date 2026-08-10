package cafe.woden.ircclient.ui.chat.embed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreview;
import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreviewHttp;
import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreviewHttpHeaders;
import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreviewHttpResponse;
import cafe.woden.ircclient.ui.chat.embed.spi.OEmbedLinkPreviewProvider;
import cafe.woden.ircclient.ui.chat.embed.spi.OEmbedResponseFields;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import org.junit.jupiter.api.Test;

class OEmbedLinkPreviewResolverTest {

  @Test
  void resolvesMatchingProviderResponse() {
    RecordingHttp http =
        new RecordingHttp(
            """
        {"title":"Example title","author_name":"Example Author","provider_name":"Example Site","thumbnail_url":"https://cdn.example/thumb.png"}
        """);
    OEmbedLinkPreviewResolver resolver =
        new OEmbedLinkPreviewResolver(java.util.List.of(provider()));

    LinkPreview preview =
        resolver.tryResolve(
            URI.create("https://oembed.example/post/42"), "https://oembed.example/post/42", http);

    assertEquals(
        URI.create("https://api.example/oembed?url=https://oembed.example/post/42"),
        http.requestedUri);
    assertEquals("application/json+oembed,application/json", http.accept);
    assertEquals("Example title", preview.title());
    assertEquals("by Example Author", preview.description());
    assertEquals("Example Site", preview.siteName());
    assertEquals("https://cdn.example/thumb.png", preview.imageUrl());
    assertEquals(1, preview.mediaCount());
  }

  @Test
  void usesProviderFallbacksWhenResponseOmitsTitleAndProviderName() {
    RecordingHttp http =
        new RecordingHttp(
            """
        {"author_name":"Example Author"}
        """);
    OEmbedLinkPreviewResolver resolver =
        new OEmbedLinkPreviewResolver(java.util.List.of(provider()));

    LinkPreview preview =
        resolver.tryResolve(
            URI.create("https://oembed.example/post/42"), "https://oembed.example/post/42", http);

    assertEquals("Fallback title", preview.title());
    assertEquals("Fallback Site", preview.siteName());
    assertEquals("by Example Author", preview.description());
  }

  @Test
  void returnsNullWhenNoProviderMatches() {
    OEmbedLinkPreviewResolver resolver =
        new OEmbedLinkPreviewResolver(java.util.List.of(provider()));

    LinkPreview preview =
        resolver.tryResolve(
            URI.create("https://other.example/post/42"),
            "https://other.example/post/42",
            new RecordingHttp("{}"));

    assertNull(preview);
  }

  private static OEmbedLinkPreviewProvider provider() {
    return new OEmbedLinkPreviewProvider() {
      @Override
      public String id() {
        return "example";
      }

      @Override
      public boolean matches(URI uri) {
        return uri != null && "oembed.example".equals(uri.getHost());
      }

      @Override
      public URI endpointFor(URI uri, String originalUrl) {
        return URI.create("https://api.example/oembed?url=" + originalUrl);
      }

      @Override
      public String defaultSiteName() {
        return "Fallback Site";
      }

      @Override
      public String titleFallback(OEmbedResponseFields fields) {
        return "Fallback title";
      }
    };
  }

  private static final class RecordingHttp implements LinkPreviewHttp {
    private final String body;
    private URI requestedUri;
    private String accept;

    private RecordingHttp(String body) {
      this.body = body;
    }

    @Override
    public LinkPreviewHttpResponse<InputStream> getStream(URI uri, String accept) {
      return new LinkPreviewHttpResponse<>(
          200, new LinkPreviewHttpHeaders(Map.of()), new ByteArrayInputStream(new byte[0]));
    }

    @Override
    public LinkPreviewHttpResponse<InputStream> getStream(
        URI uri, String accept, Map<String, String> extraHeaders) {
      return getStream(uri, accept);
    }

    @Override
    public LinkPreviewHttpResponse<String> getString(URI uri) throws IOException {
      return getString(uri, null, null);
    }

    @Override
    public LinkPreviewHttpResponse<String> getString(URI uri, Map<String, String> extraHeaders)
        throws IOException {
      return getString(uri, null, extraHeaders);
    }

    @Override
    public LinkPreviewHttpResponse<String> getString(
        URI uri, String accept, Map<String, String> extraHeaders) {
      this.requestedUri = uri;
      this.accept = accept;
      return new LinkPreviewHttpResponse<>(200, new LinkPreviewHttpHeaders(Map.of()), body);
    }
  }
}
