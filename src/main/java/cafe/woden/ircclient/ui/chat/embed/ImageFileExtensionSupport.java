package cafe.woden.ircclient.ui.chat.embed;

import java.net.URI;
import java.util.List;
import java.util.Locale;

final class ImageFileExtensionSupport {

  private ImageFileExtensionSupport() {}

  static String extensionFromUrl(String url) {
    return extensionFromUrl(url, List.of());
  }

  static String extensionFromUrl(String url, List<ImageUrlExtensionProvider> extensionProviders) {
    try {
      String path = URI.create(url).getPath();
      if (path == null) return ".img";
      String lower = path.toLowerCase(Locale.ROOT);
      for (String extension : ImageUrlExtractor.imageExtensions(extensionProviders)) {
        if (lower.endsWith(extension)) return extension;
      }
    } catch (Exception ignored) {
    }
    return ".img";
  }
}
