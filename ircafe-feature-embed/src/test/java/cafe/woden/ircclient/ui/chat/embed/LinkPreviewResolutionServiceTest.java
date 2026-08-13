package cafe.woden.ircclient.ui.chat.embed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreview;
import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreviewHttp;
import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreviewResolver;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class LinkPreviewResolutionServiceTest {

  private final LinkPreviewResolutionService service = new LinkPreviewResolutionService();

  @Test
  void continuesAfterResolverFailureAndReturnsFirstMatch() {
    LinkPreviewFetchRequest request = request("https://example.com/post");
    RuntimeException failure = new RuntimeException("boom");
    LinkPreview expected =
        new LinkPreview(request.normalizedUrl(), "Title", null, "Example", null, 0);
    LinkPreviewResolver throwing =
        (uri, originalUrl, http) -> {
          throw failure;
        };
    LinkPreviewResolver missing = (uri, originalUrl, http) -> null;
    LinkPreviewResolver matching = (uri, originalUrl, http) -> expected;

    LinkPreviewResolutionResult result =
        service.resolve(request, new FakeLinkPreviewHttp(), List.of(throwing, missing, matching));

    assertTrue(result.matched());
    assertSame(expected, result.preview());
    assertEquals(1, result.failures().size());
    assertSame(throwing, result.failures().getFirst().resolver());
    assertEquals(request.normalizedUrl(), result.failures().getFirst().normalizedUrl());
    assertSame(failure, result.failures().getFirst().error());
  }

  @Test
  void returnsNoMatchWithCapturedFailures() {
    LinkPreviewFetchRequest request = request("https://example.com/post");
    LinkPreviewResolver throwing =
        (uri, originalUrl, http) -> {
          throw new IllegalStateException("not today");
        };

    LinkPreviewResolutionResult result =
        service.resolve(
            request,
            new FakeLinkPreviewHttp(),
            Arrays.asList(throwing, (LinkPreviewResolver) null));

    assertFalse(result.matched());
    assertEquals(1, result.failures().size());
    assertSame(throwing, result.failures().getFirst().resolver());
  }

  @Test
  void rejectsMissingRequestOrHttpFacade() {
    LinkPreviewFetchRequest request = request("https://example.com/post");

    assertThrows(
        IllegalArgumentException.class,
        () -> service.resolve(null, new FakeLinkPreviewHttp(), List.of()));
    assertThrows(IllegalArgumentException.class, () -> service.resolve(request, null, List.of()));
  }

  private static LinkPreviewFetchRequest request(String url) {
    URI uri = URI.create(url);
    return new LinkPreviewFetchRequest("server-a", url, url, uri);
  }

  private static final class FakeLinkPreviewHttp implements LinkPreviewHttp {
    @Override
    public cafe.woden.ircclient.ui.chat.embed.spi.LinkPreviewHttpResponse<java.io.InputStream>
        getStream(URI uri, String accept) {
      throw new AssertionError("unexpected HTTP call");
    }

    @Override
    public cafe.woden.ircclient.ui.chat.embed.spi.LinkPreviewHttpResponse<java.io.InputStream>
        getStream(URI uri, String accept, java.util.Map<String, String> extraHeaders) {
      throw new AssertionError("unexpected HTTP call");
    }

    @Override
    public cafe.woden.ircclient.ui.chat.embed.spi.LinkPreviewHttpResponse<String> getString(
        URI uri) {
      throw new AssertionError("unexpected HTTP call");
    }

    @Override
    public cafe.woden.ircclient.ui.chat.embed.spi.LinkPreviewHttpResponse<String> getString(
        URI uri, java.util.Map<String, String> extraHeaders) {
      throw new AssertionError("unexpected HTTP call");
    }

    @Override
    public cafe.woden.ircclient.ui.chat.embed.spi.LinkPreviewHttpResponse<String> getString(
        URI uri, String accept, java.util.Map<String, String> extraHeaders) {
      throw new AssertionError("unexpected HTTP call");
    }
  }
}
