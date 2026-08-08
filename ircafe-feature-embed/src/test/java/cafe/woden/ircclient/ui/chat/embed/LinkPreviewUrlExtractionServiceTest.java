package cafe.woden.ircclient.ui.chat.embed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.ui.chat.embed.spi.ImageUrlExtensionProvider;
import java.util.List;
import org.junit.jupiter.api.Test;

class LinkPreviewUrlExtractionServiceTest {

  private final LinkPreviewUrlExtractionService service = new LinkPreviewUrlExtractionService();

  @Test
  void extractsDirectImageUrlsAndNormalizesWwwUrls() {
    ImageUrlExtensionProvider provider = () -> List.of("png", ".webp");

    assertEquals(
        List.of("https://www.example.com/photo.PNG", "https://cdn.example/banner.webp"),
        service.extractImageUrls(
            "images www.example.com/photo.PNG, https://cdn.example/banner.webp)",
            List.of(provider)));
  }

  @Test
  void extractsPreviewUrlsWhileExcludingDirectImages() {
    ImageUrlExtensionProvider provider = () -> List.of("jxl");

    assertEquals(
        List.of("https://example.com/post"),
        service.extractPreviewUrls(
            "image https://cdn.example/artwork.jxl page https://example.com/post",
            List.of(provider)));
  }

  @Test
  void resolvesExtensionForImageTempFiles() {
    ImageUrlExtensionProvider provider = () -> List.of("jxl");

    assertEquals(
        ".jxl",
        service.extensionFromUrl("https://cdn.example/artwork.jxl?download=1", List.of(provider)));
    assertEquals(
        ".img",
        service.extensionFromUrl("https://cdn.example/artwork.unknown", List.of(provider)));
  }

  @Test
  void isolatesBrokenImageExtensionProviders() {
    ImageUrlExtensionProvider broken =
        () -> {
          throw new IllegalStateException("boom");
        };

    assertTrue(service.imageExtensions(List.of(broken)).isEmpty());
  }
}
