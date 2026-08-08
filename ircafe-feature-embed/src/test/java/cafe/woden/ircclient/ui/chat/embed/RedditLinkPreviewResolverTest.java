package cafe.woden.ircclient.ui.chat.embed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreview;
import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreviewHttp;
import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreviewHttpHeaders;
import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreviewHttpResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RedditLinkPreviewResolverTest {

  private final RedditLinkPreviewResolver resolver = new RedditLinkPreviewResolver();

  @Test
  void resolvesRedditPostJson() {
    URI postUri = URI.create("https://www.reddit.com/r/java/comments/abc123/title_slug/");

    LinkPreview preview =
        resolver.tryResolve(
            postUri,
            postUri.toString(),
            new FakeLinkPreviewHttp(
                URI.create(
                    "https://www.reddit.com/r/java/comments/abc123/title_slug.json?raw_json=1"),
                "application/json",
                redditListingJson()));

    assertEquals("Feature-owned Reddit previews", preview.title());
    assertEquals("Reddit", preview.siteName());
    assertEquals("https://www.reddit.com/r/java/comments/abc123/title_slug/", preview.url());
    assertEquals("https://preview.redd.it/image.jpg?width=640&crop=smart", preview.imageUrl());
    assertEquals(1, preview.mediaCount());
    assertTrue(preview.description().startsWith("r/java • u/alice\nThis is the post body."));
  }

  @Test
  void ignoresNonPostRedditUrls() {
    URI uri = URI.create("https://www.reddit.com/r/java/");

    assertNull(resolver.tryResolve(uri, uri.toString(), new NeverCalledLinkPreviewHttp()));
  }

  @Test
  void supportsShortCommentsPermalinkShape() {
    URI uri = URI.create("https://reddit.com/comments/abc123/title_slug");

    assertTrue(RedditLinkPreviewResolver.looksLikePostPath(uri.getPath()));
    assertEquals(
        URI.create("https://reddit.com/comments/abc123/title_slug.json?raw_json=1"),
        RedditLinkPreviewResolver.redditJsonUri(uri));
  }

  private static String redditListingJson() {
    return """
        [
          {
            "data": {
              "children": [
                {
                  "data": {
                    "title": "Feature-owned Reddit previews",
                    "subreddit_name_prefixed": "r/java",
                    "author": "alice",
                    "selftext": "This is the post body. It has enough detail for a preview.",
                    "permalink": "/r/java/comments/abc123/title_slug/",
                    "preview": {
                      "images": [
                        {
                          "source": {
                            "url": "https://preview.redd.it/image.jpg?width=640&amp;crop=smart"
                          }
                        }
                      ]
                    },
                    "thumbnail": "self"
                  }
                }
              ]
            }
          }
        ]
        """;
  }

  private record FakeLinkPreviewHttp(URI expectedUri, String expectedAccept, String body)
      implements LinkPreviewHttp {
    @Override
    public LinkPreviewHttpResponse<InputStream> getStream(URI uri, String accept)
        throws IOException {
      throw new IOException("unexpected stream fetch");
    }

    @Override
    public LinkPreviewHttpResponse<InputStream> getStream(
        URI uri, String accept, Map<String, String> extraHeaders) throws IOException {
      throw new IOException("unexpected stream fetch");
    }

    @Override
    public LinkPreviewHttpResponse<String> getString(URI uri) throws IOException {
      throw new IOException("unexpected string fetch without accept");
    }

    @Override
    public LinkPreviewHttpResponse<String> getString(URI uri, Map<String, String> extraHeaders)
        throws IOException {
      throw new IOException("unexpected string fetch without accept");
    }

    @Override
    public LinkPreviewHttpResponse<String> getString(
        URI uri, String accept, Map<String, String> extraHeaders) {
      assertEquals(expectedUri, uri);
      assertEquals(expectedAccept, accept);
      return new LinkPreviewHttpResponse<>(200, new LinkPreviewHttpHeaders(Map.of()), body);
    }
  }

  private static final class NeverCalledLinkPreviewHttp implements LinkPreviewHttp {
    @Override
    public LinkPreviewHttpResponse<InputStream> getStream(URI uri, String accept)
        throws IOException {
      throw new IOException("unexpected stream fetch");
    }

    @Override
    public LinkPreviewHttpResponse<InputStream> getStream(
        URI uri, String accept, Map<String, String> extraHeaders) throws IOException {
      throw new IOException("unexpected stream fetch");
    }

    @Override
    public LinkPreviewHttpResponse<String> getString(URI uri) throws IOException {
      throw new IOException("unexpected string fetch");
    }

    @Override
    public LinkPreviewHttpResponse<String> getString(URI uri, Map<String, String> extraHeaders)
        throws IOException {
      throw new IOException("unexpected string fetch");
    }

    @Override
    public LinkPreviewHttpResponse<String> getString(
        URI uri, String accept, Map<String, String> extraHeaders) throws IOException {
      throw new IOException("unexpected string fetch");
    }
  }
}
