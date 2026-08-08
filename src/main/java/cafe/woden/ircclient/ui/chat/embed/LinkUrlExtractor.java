package cafe.woden.ircclient.ui.chat.embed;

import java.util.List;

/**
 * Best-effort extraction of normal web URLs from chat text.
 *
 * <p>We intentionally keep this permissive (like {@link ImageUrlExtractor}): the transcript already
 * shows the raw URL text; this is only used to decide if we should append a preview card.
 */
final class LinkUrlExtractor {

  private static final LinkPreviewUrlExtractionService URL_EXTRACTION =
      new LinkPreviewUrlExtractionService();

  private LinkUrlExtractor() {}

  static List<String> extractUrls(String text) {
    return extractUrls(text, List.of());
  }

  static List<String> extractUrls(
      String text,
      List<? extends cafe.woden.ircclient.ui.chat.embed.spi.ImageUrlExtensionProvider>
          imageExtensionProviders) {
    return URL_EXTRACTION.extractPreviewUrls(text, imageExtensionProviders);
  }
}
