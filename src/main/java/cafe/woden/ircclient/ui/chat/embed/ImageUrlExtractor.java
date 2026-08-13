package cafe.woden.ircclient.ui.chat.embed;

import java.util.List;
import java.util.Set;

final class ImageUrlExtractor {

  private static final LinkPreviewUrlExtractionService URL_EXTRACTION =
      new LinkPreviewUrlExtractionService();

  private ImageUrlExtractor() {}

  static List<String> extractImageUrls(String text) {
    return extractImageUrls(text, List.of());
  }

  static List<String> extractImageUrls(
      String text,
      List<? extends cafe.woden.ircclient.ui.chat.embed.spi.ImageUrlExtensionProvider>
          extensionProviders) {
    return URL_EXTRACTION.extractImageUrls(text, extensionProviders);
  }

  static Set<String> imageExtensions(
      List<? extends cafe.woden.ircclient.ui.chat.embed.spi.ImageUrlExtensionProvider>
          extensionProviders) {
    return URL_EXTRACTION.imageExtensions(extensionProviders);
  }
}
