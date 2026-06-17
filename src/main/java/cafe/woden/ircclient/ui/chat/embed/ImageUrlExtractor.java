package cafe.woden.ircclient.ui.chat.embed;

import cafe.woden.ircclient.ui.chat.render.ChatRichTextRenderer;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class ImageUrlExtractor {

  // Keep in sync with ChatRichTextRenderer.
  private static final Pattern URL_PATTERN = Pattern.compile("(https?://\\S+|www\\.\\S+)");

  private ImageUrlExtractor() {}

  static List<String> extractImageUrls(String text) {
    return extractImageUrls(text, List.of());
  }

  static List<String> extractImageUrls(
      String text,
      List<? extends cafe.woden.ircclient.ui.chat.embed.spi.ImageUrlExtensionProvider>
          extensionProviders) {
    if (text == null || text.isBlank()) return List.of();

    Set<String> imageExtensions = imageExtensions(extensionProviders);
    Matcher m = URL_PATTERN.matcher(text);
    Set<String> out = new LinkedHashSet<>();
    while (m.find()) {
      String raw = m.group(1);
      UrlParts parts = splitUrlTrailingPunct(raw);
      String url = ChatRichTextRenderer.normalizeUrl(parts.url);
      if (isLikelyDirectImageUrl(url, imageExtensions)) {
        out.add(url);
      }
    }

    return new ArrayList<>(out);
  }

  static Set<String> imageExtensions(
      List<? extends cafe.woden.ircclient.ui.chat.embed.spi.ImageUrlExtensionProvider>
          extensionProviders) {
    return ImageUrlExtensionProviders.imageExtensions(extensionProviders);
  }

  private static boolean isLikelyDirectImageUrl(String url, Set<String> imageExtensions) {
    if (url == null || url.isBlank()) return false;
    try {
      URI uri = URI.create(url);
      String scheme = uri.getScheme();
      if (scheme == null) return false;
      scheme = scheme.toLowerCase(Locale.ROOT);
      if (!scheme.equals("http") && !scheme.equals("https")) return false;

      String path = uri.getPath();
      if (path == null) return false;
      String p = path.toLowerCase(Locale.ROOT);

      for (String extension : imageExtensions) {
        if (p.endsWith(extension)) return true;
      }
      return false;
    } catch (Exception ignored) {
      return false;
    }
  }

  private static UrlParts splitUrlTrailingPunct(String raw) {
    if (raw == null || raw.isEmpty()) return new UrlParts("", "");
    int end = raw.length();
    while (end > 0) {
      char c = raw.charAt(end - 1);
      if (c == '.' || c == ',' || c == ')' || c == ']' || c == '}' || c == '!' || c == '?') {
        end--;
      } else {
        break;
      }
    }
    if (end == raw.length()) return new UrlParts(raw, "");
    return new UrlParts(raw.substring(0, end), raw.substring(end));
  }

  private record UrlParts(String url, String trailing) {}
}
