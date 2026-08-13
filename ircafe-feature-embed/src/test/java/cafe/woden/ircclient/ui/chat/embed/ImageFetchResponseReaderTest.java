package cafe.woden.ircclient.ui.chat.embed;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class ImageFetchResponseReaderTest {

  private final ImageFetchResponseReader reader = new ImageFetchResponseReader();

  @Test
  void readsImageBytesWithinLimit() throws Exception {
    byte[] bytes = new byte[] {(byte) 0x89, 'P', 'N', 'G'};

    ImageFetchReadResult result =
        reader.read(
            new ByteArrayInputStream(bytes), "image/png", "https://example.test/a.png", 0, 8);

    assertArrayEquals(bytes, result.bytes());
    assertTrue(result.retryUrl().isEmpty());
  }

  @Test
  void requestsAmazonSizedRetryWhenStreamExceedsLimit() throws Exception {
    byte[] bytes = new byte[32];

    ImageFetchReadResult result =
        reader.read(
            new ByteArrayInputStream(bytes),
            "image/jpeg",
            "https://m.media-amazon.com/images/M/poster@._V1_.jpg",
            0,
            8);

    assertEquals(
        "https://m.media-amazon.com/images/M/poster@._V1_UX512_.jpg",
        result.retryUrl().orElseThrow());
  }

  @Test
  void throwsWhenOversizeCannotRetry() {
    IOException ex =
        assertThrows(
            IOException.class,
            () ->
                reader.read(
                    new ByteArrayInputStream(new byte[32]),
                    "image/jpeg",
                    "https://cdn.example.test/poster.jpg",
                    0,
                    8));

    assertTrue(ex.getMessage().contains("Image too large"));
  }

  @Test
  void throwsHtmlResponseExceptionWithSafeSample() {
    byte[] html = "<html><body>Access denied</body></html>".getBytes(StandardCharsets.UTF_8);

    ImageFetchHtmlResponseException ex =
        assertThrows(
            ImageFetchHtmlResponseException.class,
            () ->
                reader.read(
                    new ByteArrayInputStream(html),
                    "text/html; charset=utf-8",
                    "https://example.test/image.jpg",
                    0,
                    4096));

    assertTrue(ex.sampleText().contains("Access denied"));
    assertEquals(html.length, ex.byteCount());
  }
}
