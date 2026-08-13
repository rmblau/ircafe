package cafe.woden.ircclient.ui.chat.embed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

import cafe.woden.ircclient.ui.chat.embed.spi.EmbedHttpHeaderProvider;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ImageFetchHttpHeadersTest {

  private final ImageFetchHttpHeaders imageHeaders = new ImageFetchHttpHeaders();

  @Test
  void buildsDefaultImageFetchHeadersWithoutAvif() {
    LinkPreviewHttpHeaderResult result =
        imageHeaders.headersFor(URI.create("https://images.example.test/poster.png"), List.of());

    assertEquals("ircafe-link-preview/1.0", result.headers().get("User-Agent"));
    assertEquals("en-US,en;q=0.9", result.headers().get("Accept-Language"));
    assertEquals("gzip", result.headers().get("Accept-Encoding"));
    assertEquals(
        "image/jpeg,image/png,image/webp,image/gif,image/*;q=0.5,*/*;q=0.4",
        result.headers().get("Accept"));
    assertEquals("image", result.headers().get("Sec-Fetch-Dest"));
    assertEquals("no-cors", result.headers().get("Sec-Fetch-Mode"));
    assertFalse(result.headers().get("Accept").contains("avif"));
    assertEquals(List.of(), result.failures());
  }

  @Test
  void addsImdbRefererForAmazonImageHosts() {
    LinkPreviewHttpHeaderResult result =
        imageHeaders.headersFor(
            URI.create("https://m.media-amazon.com/images/M/poster.jpg"), List.of());

    assertEquals("https://www.imdb.com/", result.headers().get("Referer"));
  }

  @Test
  void addsInstagramRefererAndBrowserUserAgentForInstagramMediaHosts() {
    LinkPreviewHttpHeaderResult result =
        imageHeaders.headersFor(
            URI.create("https://scontent.cdninstagram.com/v/t51.29350-15/post.jpg"), List.of());

    assertEquals("https://www.instagram.com/", result.headers().get("Referer"));
    assertEquals(LinkPreviewHttpDefaults.BROWSER_USER_AGENT, result.headers().get("User-Agent"));
  }

  @Test
  void providerHeadersOverrideBuiltInImageDefaults() {
    EmbedHttpHeaderProvider provider =
        uri -> Map.of("Referer", "https://provider.example/", "X-Image-Plugin", "yes");

    LinkPreviewHttpHeaderResult result =
        imageHeaders.headersFor(
            URI.create("https://m.media-amazon.com/images/M/poster.jpg"), List.of(provider));

    assertEquals("https://provider.example/", result.headers().get("Referer"));
    assertEquals("yes", result.headers().get("X-Image-Plugin"));
  }

  @Test
  void providerFailuresAreReturnedForRootAdapterLogging() {
    RuntimeException boom = new IllegalStateException("boom");
    EmbedHttpHeaderProvider broken =
        uri -> {
          throw boom;
        };

    LinkPreviewHttpHeaderResult result =
        imageHeaders.headersFor(
            URI.create("https://images.example.test/poster.png"), List.of(broken));

    assertEquals(1, result.failures().size());
    assertSame(broken, result.failures().get(0).provider());
    assertSame(boom, result.failures().get(0).error());
  }
}
