package cafe.woden.ircclient.ui.chat.embed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreview;
import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreviewHttp;
import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreviewHttpHeaders;
import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreviewHttpResponse;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.Test;

class MastodonStatusApiPreviewResolverTest {

  private final MastodonStatusApiPreviewResolver resolver = new MastodonStatusApiPreviewResolver();

  @Test
  void resolvesMastodonStatusApiResponse() throws Exception {
    URI postUri = URI.create("https://mastodon.social/@alice/123456789");
    String json =
        """
        {
          "content": "<p>Hello <strong>IRCafe</strong> friends.</p>",
          "spoiler_text": "",
          "sensitive": false,
          "account": {
            "display_name": "Alice Example",
            "acct": "alice@mastodon.social",
            "username": "alice"
          },
          "media_attachments": [
            {
              "preview_url": "https://cdn.example/preview.jpg",
              "url": "https://cdn.example/full.jpg"
            }
          ]
        }
        """;

    LinkPreview preview =
        resolver.tryResolve(
            postUri,
            postUri.toString(),
            new FakeLinkPreviewHttp(
                URI.create("https://mastodon.social/api/v1/statuses/123456789"), json));

    assertEquals(postUri.toString(), preview.url());
    assertEquals("Post by Alice Example", preview.title());
    assertEquals("mastodon.social", preview.siteName());
    assertEquals("https://cdn.example/preview.jpg", preview.imageUrl());
    assertEquals(1, preview.mediaCount());
    assertTrue(preview.description().contains("Hello IRCafe friends."));
  }

  @Test
  void hidesSensitiveMastodonMedia() throws Exception {
    URI postUri = URI.create("https://mastodon.social/users/alice/statuses/123456789");
    String json =
        """
        {
          "content": "<p>behind a content warning</p>",
          "spoiler_text": "spoiler here",
          "sensitive": true,
          "account": {"username": "alice"},
          "media_attachments": [{"preview_url": "https://cdn.example/preview.jpg"}]
        }
        """;

    LinkPreview preview =
        resolver.tryResolve(
            postUri,
            postUri.toString(),
            new FakeLinkPreviewHttp(
                URI.create("https://mastodon.social/api/v1/statuses/123456789"), json));

    assertNull(preview.imageUrl());
    assertEquals(1, preview.mediaCount());
    assertTrue(preview.description().contains("CW: spoiler here"));
  }

  @Test
  void ignoresNonStatusPaths() throws Exception {
    URI uri = URI.create("https://mastodon.social/about");

    LinkPreview preview =
        resolver.tryResolve(
            uri,
            uri.toString(),
            new FakeLinkPreviewHttp(URI.create("https://mastodon.social/about"), "{}"));

    assertNull(preview);
  }

  private record FakeLinkPreviewHttp(URI expectedUri, String body) implements LinkPreviewHttp {
    @Override
    public LinkPreviewHttpResponse<InputStream> getStream(URI uri, String accept) {
      return getStream(uri, accept, null);
    }

    @Override
    public LinkPreviewHttpResponse<InputStream> getStream(
        URI uri, String accept, Map<String, String> headers) {
      assertEquals(expectedUri, uri);
      return new LinkPreviewHttpResponse<>(200, jsonHeaders(), stream(body));
    }

    @Override
    public LinkPreviewHttpResponse<String> getString(URI uri) {
      return getString(uri, null, null);
    }

    @Override
    public LinkPreviewHttpResponse<String> getString(URI uri, Map<String, String> headers) {
      return getString(uri, null, headers);
    }

    @Override
    public LinkPreviewHttpResponse<String> getString(
        URI uri, String accept, Map<String, String> headers) {
      assertEquals(expectedUri, uri);
      return new LinkPreviewHttpResponse<>(200, jsonHeaders(), body);
    }

    private static LinkPreviewHttpHeaders jsonHeaders() {
      return new LinkPreviewHttpHeaders(
          Map.of("Content-Type", java.util.List.of("application/json")));
    }

    private static InputStream stream(String body) {
      return new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));
    }
  }
}
