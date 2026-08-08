package cafe.woden.ircclient.ui.chat.embed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreview;
import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreviewHttp;
import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreviewHttpHeaders;
import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreviewHttpResponse;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import org.junit.jupiter.api.Test;

class WikipediaPreviewUtilTest {

  @Test
  void resolvesSummaryApiPreview() {
    WikipediaLinkPreviewResolver resolver = new WikipediaLinkPreviewResolver();
    URI articleUri = URI.create("https://en.wikipedia.org/wiki/Internet_Relay_Chat");
    String responseJson =
        """
        {
          "title": "Internet Relay Chat",
          "extract": "Internet Relay Chat is a text-based chat system for instant messaging.",
          "thumbnail": {
            "source": "https://upload.wikimedia.org/wikipedia/commons/thumb/example.png"
          },
          "content_urls": {
            "desktop": {
              "page": "https://en.wikipedia.org/wiki/Internet_Relay_Chat"
            }
          }
        }
        """;

    LinkPreview preview =
        resolver.tryResolve(
            articleUri,
            articleUri.toString(),
            new FakeLinkPreviewHttp(
                URI.create(
                    "https://en.wikipedia.org/api/rest_v1/page/summary/Internet%20Relay%20Chat"),
                "application/json",
                responseJson));

    assertEquals("Internet Relay Chat", preview.title());
    assertEquals("Wikipedia", preview.siteName());
    assertEquals("https://en.wikipedia.org/wiki/Internet_Relay_Chat", preview.url());
    assertEquals(
        "https://upload.wikimedia.org/wikipedia/commons/thumb/example.png", preview.imageUrl());
    assertTrue(preview.description().contains("text-based chat system"));
  }

  @Test
  void recognizesMobileArticleUrlsForUiExpansion() {
    assertTrue(WikipediaPreviewUtil.isWikipediaArticleUrl("https://en.m.wikipedia.org/wiki/IRC"));
    assertEquals(
        URI.create("https://en.wikipedia.org/api/rest_v1/page/summary/IRC"),
        WikipediaPreviewUtil.toSummaryApiUri(URI.create("https://en.m.wikipedia.org/wiki/IRC")));
  }

  private record FakeLinkPreviewHttp(URI expectedUri, String expectedAccept, String body)
      implements LinkPreviewHttp {
    @Override
    public LinkPreviewHttpResponse<InputStream> getStream(URI uri, String accept) {
      throw new AssertionError("unexpected HTTP call");
    }

    @Override
    public LinkPreviewHttpResponse<InputStream> getStream(
        URI uri, String accept, Map<String, String> extraHeaders) {
      throw new AssertionError("unexpected HTTP call");
    }

    @Override
    public LinkPreviewHttpResponse<String> getString(URI uri) {
      throw new AssertionError("unexpected HTTP call");
    }

    @Override
    public LinkPreviewHttpResponse<String> getString(URI uri, Map<String, String> extraHeaders) {
      throw new AssertionError("unexpected HTTP call");
    }

    @Override
    public LinkPreviewHttpResponse<String> getString(
        URI uri, String accept, Map<String, String> extraHeaders) {
      assertEquals(expectedUri, uri);
      assertEquals(expectedAccept, accept);
      return new LinkPreviewHttpResponse<>(200, new LinkPreviewHttpHeaders(Map.of()), body);
    }
  }
}
