package cafe.woden.ircclient.ui.chat.embed;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.ui.chat.embed.spi.EmbedHttpHeaderProvider;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ImageFetchDownloadServiceTest {

  @Test
  void downloadsImageBytesThroughHttpPortWithFeatureHeaders() throws Exception {
    RecordingHttpClient client =
        new RecordingHttpClient(
            response(200, Map.of("content-type", List.of("image/png")), new byte[] {1, 2, 3}));
    ImageFetchDownloadService service = new ImageFetchDownloadService(client);

    byte[] bytes = service.download("libera", "https://cdn.example.test/image.png");

    assertArrayEquals(new byte[] {1, 2, 3}, bytes);
    assertEquals("libera", client.serverIds.getFirst());
    assertEquals(URI.create("https://cdn.example.test/image.png"), client.uris.getFirst());
    assertTrue(client.requestHeaders.getFirst().containsKey("User-Agent"));
    assertTrue(client.requestHeaders.getFirst().containsKey("Accept"));
  }

  @Test
  void retriesAmazonHttpErrorsThroughTheSameHttpPort() throws Exception {
    RecordingHttpClient client =
        new RecordingHttpClient(
            response(403, Map.of("content-type", List.of("text/html")), new byte[] {}),
            response(200, Map.of("content-type", List.of("image/jpeg")), new byte[] {4, 5, 6}));
    ImageFetchDownloadService service = new ImageFetchDownloadService(client);

    byte[] bytes =
        service.download("libera", "https://m.media-amazon.com/images/M/poster@._V1_UX512_.jpg");

    assertArrayEquals(new byte[] {4, 5, 6}, bytes);
    assertEquals(
        URI.create("https://m.media-amazon.com/images/M/poster@._V1_UX512_.jpg"),
        client.uris.get(0));
    assertEquals(
        URI.create("https://m.media-amazon.com/images/M/poster@._V1_.jpg"), client.uris.get(1));
  }

  @Test
  void rejectsUnsupportedSchemesBeforeUsingHttpPort() {
    RecordingHttpClient client = new RecordingHttpClient();
    ImageFetchDownloadService service = new ImageFetchDownloadService(client);

    IOException ex =
        assertThrows(IOException.class, () -> service.download("libera", "file:///tmp/image.png"));

    assertTrue(ex.getMessage().contains("Unsupported URL scheme"));
    assertTrue(client.uris.isEmpty());
  }

  @Test
  void reportsHeaderProviderFailuresWhileStillDownloading() throws Exception {
    RecordingHttpClient client =
        new RecordingHttpClient(
            response(200, Map.of("content-type", List.of("image/png")), new byte[] {9}));
    List<LinkPreviewHttpHeaderProviderFailure> failures = new ArrayList<>();
    EmbedHttpHeaderProvider provider =
        uri -> {
          throw new IllegalStateException("boom");
        };
    ImageFetchDownloadService service =
        new ImageFetchDownloadService(
            client,
            List.of(provider),
            new ImageFetchHttpHeaders(),
            new ImageFetchResponseReader(),
            new ImageFetchResponsePolicy(),
            1024,
            failures::add);

    byte[] bytes = service.download("libera", "https://cdn.example.test/image.png");

    assertArrayEquals(new byte[] {9}, bytes);
    assertFalse(failures.isEmpty());
    assertEquals(provider, failures.getFirst().provider());
  }

  private static ImageFetchHttpResponse response(
      int code, Map<String, List<String>> headers, byte[] body) {
    return new ImageFetchHttpResponse(code, headers, new ByteArrayInputStream(body));
  }

  private static final class RecordingHttpClient implements ImageFetchHttpClient {
    private final List<ImageFetchHttpResponse> responses;
    private final List<String> serverIds = new ArrayList<>();
    private final List<URI> uris = new ArrayList<>();
    private final List<Map<String, String>> requestHeaders = new ArrayList<>();

    RecordingHttpClient(ImageFetchHttpResponse... responses) {
      this.responses = new ArrayList<>(List.of(responses));
    }

    @Override
    public ImageFetchHttpResponse getStream(
        String serverId, URI uri, Map<String, String> requestHeaders) throws IOException {
      serverIds.add(serverId);
      uris.add(uri);
      this.requestHeaders.add(new LinkedHashMap<>(requestHeaders));
      if (responses.isEmpty()) {
        throw new IOException("no test response queued");
      }
      return responses.removeFirst();
    }
  }
}
