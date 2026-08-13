package cafe.woden.ircclient.ui.chat.embed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import cafe.woden.ircclient.ui.chat.embed.spi.EmbedHttpHeaderProvider;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class LinkPreviewHttpAdapterHeadersTest {

  private final LinkPreviewHttpAdapterHeaders adapterHeaders = new LinkPreviewHttpAdapterHeaders();

  @Test
  void buildsDefaultPreviewHttpHeaders() {
    LinkPreviewHttpHeaderResult result =
        adapterHeaders.headersFor(URI.create("https://example.test/card"), null, null, List.of());

    assertEquals("ircafe-link-preview/1.0", result.headers().get("User-Agent"));
    assertEquals("en-US,en;q=0.9", result.headers().get("Accept-Language"));
    assertEquals("gzip", result.headers().get("Accept-Encoding"));
    assertEquals(
        "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
        result.headers().get("Accept"));
    assertEquals(List.of(), result.failures());
  }

  @Test
  void providerHeadersAreAppliedBeforeExplicitResolverHeaders() {
    EmbedHttpHeaderProvider provider = uri -> Map.of("Referer", "https://provider.example/");

    LinkPreviewHttpHeaderResult result =
        adapterHeaders.headersFor(
            URI.create("https://example.test/card"),
            "application/json",
            Map.of("Referer", "https://resolver.example/", "X-Explicit", "yes"),
            List.of(provider));

    assertEquals("application/json", result.headers().get("Accept"));
    assertEquals("https://resolver.example/", result.headers().get("Referer"));
    assertEquals("yes", result.headers().get("X-Explicit"));
  }

  @Test
  void providerFailuresAreReturnedForRootAdapterLogging() {
    RuntimeException boom = new IllegalStateException("boom");
    EmbedHttpHeaderProvider broken =
        uri -> {
          throw boom;
        };

    LinkPreviewHttpHeaderResult result =
        adapterHeaders.headersFor(
            URI.create("https://example.test/card"), "text/html", null, List.of(broken));

    assertEquals(1, result.failures().size());
    assertSame(broken, result.failures().get(0).provider());
    assertSame(boom, result.failures().get(0).error());
  }
}
