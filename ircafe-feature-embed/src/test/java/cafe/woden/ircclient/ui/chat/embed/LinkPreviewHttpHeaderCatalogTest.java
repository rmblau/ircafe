package cafe.woden.ircclient.ui.chat.embed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import cafe.woden.ircclient.ui.chat.embed.spi.EmbedHttpHeaderProvider;
import java.net.URI;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class LinkPreviewHttpHeaderCatalogTest {

  private final LinkPreviewHttpHeaderCatalog catalog = new LinkPreviewHttpHeaderCatalog();

  @Test
  void dedupesHeaderProvidersByImplementationClass() {
    CountingHeaderProvider first = new CountingHeaderProvider();
    CountingHeaderProvider duplicate = new CountingHeaderProvider();
    OtherHeaderProvider other = new OtherHeaderProvider();

    assertEquals(
        List.of(first, other),
        catalog.headerProviders(List.of(first, duplicate), Arrays.asList(other, null)));
  }

  @Test
  void appliesTrimmedProviderHeadersAndIgnoresBlankEntries() {
    Map<String, String> baseHeaders = new LinkedHashMap<>();
    baseHeaders.put(" User-Agent ", " IRCafe ");
    baseHeaders.put("X-Blank", " ");
    EmbedHttpHeaderProvider provider =
        uri -> {
          Map<String, String> headers = new LinkedHashMap<>();
          headers.put(" X-Card ", " yes ");
          headers.put(" ", "ignored");
          headers.put("X-Ignored", null);
          headers.put("User-Agent", "PluginAgent");
          return headers;
        };

    LinkPreviewHttpHeaderResult result =
        catalog.applyProviderHeaders(
            baseHeaders, URI.create("https://example.test/card"), List.of(provider));

    assertEquals("PluginAgent", result.headers().get("User-Agent"));
    assertEquals("yes", result.headers().get("X-Card"));
    assertEquals(false, result.headers().containsKey("X-Blank"));
    assertEquals(List.of(), result.failures());
  }

  @Test
  void capturesProviderFailuresAndContinuesLaterProviders() {
    RuntimeException boom = new IllegalStateException("boom");
    EmbedHttpHeaderProvider broken =
        uri -> {
          throw boom;
        };
    EmbedHttpHeaderProvider later = uri -> Map.of("X-Later", "ok");

    LinkPreviewHttpHeaderResult result =
        catalog.applyProviderHeaders(
            Map.of(), URI.create("https://example.test/card"), List.of(broken, later));

    assertEquals("ok", result.headers().get("X-Later"));
    assertEquals(1, result.failures().size());
    assertSame(broken, result.failures().get(0).provider());
    assertSame(boom, result.failures().get(0).error());
  }

  @Test
  void appliesDuplicateProviderClassOnlyOnce() {
    AtomicInteger calls = new AtomicInteger();
    List<EmbedHttpHeaderProvider> providers =
        List.of(new CountingHeaderProvider(calls), new CountingHeaderProvider(calls));

    LinkPreviewHttpHeaderResult result =
        catalog.applyProviderHeaders(Map.of(), URI.create("https://example.test/card"), providers);

    assertEquals(1, calls.get());
    assertEquals("1", result.headers().get("X-Header-Calls"));
  }

  private static class CountingHeaderProvider implements EmbedHttpHeaderProvider {
    private final AtomicInteger calls;

    private CountingHeaderProvider() {
      this(new AtomicInteger());
    }

    private CountingHeaderProvider(AtomicInteger calls) {
      this.calls = calls;
    }

    @Override
    public Map<String, String> embedHttpHeaders(URI uri) {
      return Map.of("X-Header-Calls", Integer.toString(calls.incrementAndGet()));
    }
  }

  private static final class OtherHeaderProvider implements EmbedHttpHeaderProvider {
    @Override
    public Map<String, String> embedHttpHeaders(URI uri) {
      return Map.of("X-Other", "1");
    }
  }
}
