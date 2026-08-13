package cafe.woden.ircclient.ui.chat.embed;

import java.util.List;

final class ImageFileExtensionSupport {

  private static final LinkPreviewUrlExtractionService URL_EXTRACTION =
      new LinkPreviewUrlExtractionService();

  private ImageFileExtensionSupport() {}

  static String extensionFromUrl(String url) {
    return extensionFromUrl(url, List.of());
  }

  static String extensionFromUrl(
      String url,
      List<? extends cafe.woden.ircclient.ui.chat.embed.spi.ImageUrlExtensionProvider>
          extensionProviders) {
    return URL_EXTRACTION.extensionFromUrl(url, extensionProviders);
  }
}
