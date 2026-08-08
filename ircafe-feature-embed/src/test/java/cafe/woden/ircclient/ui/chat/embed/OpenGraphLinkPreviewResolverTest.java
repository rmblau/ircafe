package cafe.woden.ircclient.ui.chat.embed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreviewHttp;
import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreviewHttpHeaders;
import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreviewHttpResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class OpenGraphLinkPreviewResolverTest {

  @Test
  void resolvesOpenGraphHtml() throws Exception {
    OpenGraphLinkPreviewResolver resolver = new OpenGraphLinkPreviewResolver(4096);
    LinkPreviewHttp http =
        new StaticHttp(
            200,
            "text/html; charset=utf-8",
            """
            <html>
              <head>
                <meta property='og:url' content='https://example.com/canonical'>
                <meta property='og:title' content='Example title'>
                <meta property='og:description' content='Example description'>
                <meta property='og:site_name' content='Example News'>
                <meta property='og:image' content='/img/card.png'>
              </head>
            </html>
            """);

    var preview =
        resolver.tryResolve(
            URI.create("https://example.com/story"), "https://example.com/story", http);

    assertThat(preview).isNotNull();
    assertThat(preview.url()).isEqualTo("https://example.com/canonical");
    assertThat(preview.title()).isEqualTo("Example title");
    assertThat(preview.description()).isEqualTo("Example description");
    assertThat(preview.siteName()).isEqualTo("Example News");
    assertThat(preview.imageUrl()).isEqualTo("https://example.com/img/card.png");
    assertThat(preview.mediaCount()).isEqualTo(1);
  }

  @Test
  void rejectsNonSuccessStatus() {
    OpenGraphLinkPreviewResolver resolver = new OpenGraphLinkPreviewResolver(4096);
    LinkPreviewHttp http = new StaticHttp(404, "text/html", "not found");

    assertThatThrownBy(
            () ->
                resolver.tryResolve(
                    URI.create("https://example.com/missing"),
                    "https://example.com/missing",
                    http))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("HTTP 404");
  }

  @Test
  void rejectsNonHtmlContentType() {
    OpenGraphLinkPreviewResolver resolver = new OpenGraphLinkPreviewResolver(4096);
    LinkPreviewHttp http = new StaticHttp(200, "application/json", "{}");

    assertThatThrownBy(
            () ->
                resolver.tryResolve(
                    URI.create("https://example.com/api"), "https://example.com/api", http))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("content-type not html");
  }

  private record StaticHttp(int status, String contentType, String body)
      implements LinkPreviewHttp {
    @Override
    public LinkPreviewHttpResponse<InputStream> getStream(URI uri, String accept)
        throws IOException {
      return getStream(uri, accept, Map.of());
    }

    @Override
    public LinkPreviewHttpResponse<InputStream> getStream(
        URI uri, String accept, Map<String, String> extraHeaders) throws IOException {
      return new LinkPreviewHttpResponse<>(
          status,
          new LinkPreviewHttpHeaders(Map.of("content-type", List.of(contentType))),
          new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public LinkPreviewHttpResponse<String> getString(URI uri) throws IOException {
      return getString(uri, Map.of());
    }

    @Override
    public LinkPreviewHttpResponse<String> getString(URI uri, Map<String, String> extraHeaders)
        throws IOException {
      return getString(uri, null, extraHeaders);
    }

    @Override
    public LinkPreviewHttpResponse<String> getString(
        URI uri, String accept, Map<String, String> extraHeaders) throws IOException {
      return new LinkPreviewHttpResponse<>(
          status, new LinkPreviewHttpHeaders(Map.of("content-type", List.of(contentType))), body);
    }
  }
}
