package cafe.woden.ircclient.ui.chat.embed;

import static org.assertj.core.api.Assertions.assertThat;

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

class SlashdotLinkPreviewResolverTest {

  @Test
  void resolvesSlashdotStoryWithSubmitterDateAndSummary() throws Exception {
    SlashdotLinkPreviewResolver resolver = new SlashdotLinkPreviewResolver(8192);
    LinkPreviewHttp http =
        new StaticHttp(
            200,
            "text/html; charset=utf-8",
            """
            <html>
              <head>
                <title>Example Story - Slashdot</title>
                <meta property='og:url' content='https://news.slashdot.org/story/26/06/29/123456/example-story'>
                <meta property='og:title' content='Example Story - Slashdot'>
                <meta property='og:description' content='Short fallback.'>
                <meta property='og:image' content='https://slashdot.org/story.png'>
              </head>
              <body>
                <main>
                  <article>
                    <h1>Example Story</h1>
                    <p>Posted by msmash on Monday June 29, 2026 @12:34PM from the irc-client dept.</p>
                    <p>A longer Slashdot story summary explains the IRC client migration and why the plugin boundary matters for future releases.</p>
                    <p>Additional detail confirms that the feature module should own parsing while the root app keeps UI and scheduling.</p>
                  </article>
                </main>
              </body>
            </html>
            """);

    var preview =
        resolver.tryResolve(
            URI.create("https://news.slashdot.org/story/26/06/29/123456/example-story"),
            "https://news.slashdot.org/story/26/06/29/123456/example-story",
            http);

    assertThat(preview).isNotNull();
    assertThat(preview.title()).isEqualTo("Example Story");
    assertThat(preview.siteName()).isEqualTo("Slashdot");
    assertThat(preview.imageUrl()).isEqualTo("https://slashdot.org/story.png");
    assertThat(preview.description()).contains("Submitter: msmash");
    assertThat(preview.description()).contains("Date: Monday June 29, 2026 @12:34PM");
    assertThat(preview.description()).contains("IRC client migration");
  }

  @Test
  void ignoresNonSlashdotUrls() throws Exception {
    SlashdotLinkPreviewResolver resolver = new SlashdotLinkPreviewResolver(8192);

    var preview =
        resolver.tryResolve(
            URI.create("https://example.com/story/26/06/29/123456/example-story"),
            "https://example.com/story/26/06/29/123456/example-story",
            new StaticHttp(200, "text/html", "<html></html>"));

    assertThat(preview).isNull();
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
