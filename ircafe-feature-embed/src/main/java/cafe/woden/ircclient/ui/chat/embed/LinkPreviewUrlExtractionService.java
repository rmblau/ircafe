package cafe.woden.ircclient.ui.chat.embed;

import cafe.woden.ircclient.ui.chat.embed.spi.ImageUrlExtensionProvider;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jmolecules.architecture.layered.InterfaceLayer;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/** Pure URL extraction and image-extension rules for embed/link-preview runtime paths. */
@Component
@InterfaceLayer
@Lazy
public class LinkPreviewUrlExtractionService {

  private static final Pattern URL_PATTERN = Pattern.compile("(https?://\\S+|www\\.\\S+)");

  private final LinkPreviewProviderCatalog catalog;

  public LinkPreviewUrlExtractionService() {
    this(new LinkPreviewProviderCatalog());
  }

  public LinkPreviewUrlExtractionService(LinkPreviewProviderCatalog catalog) {
    this.catalog = catalog != null ? catalog : new LinkPreviewProviderCatalog();
  }

  public List<String> extractImageUrls(
      String text, Collection<? extends ImageUrlExtensionProvider> extensionProviders) {
    return extractUrls(text, extensionProviders, true);
  }

  public List<String> extractPreviewUrls(
      String text, Collection<? extends ImageUrlExtensionProvider> extensionProviders) {
    return extractUrls(text, extensionProviders, false);
  }

  public Set<String> imageExtensions(
      Collection<? extends ImageUrlExtensionProvider> extensionProviders) {
    return catalog.imageExtensions(extensionProviders);
  }

  public String extensionFromUrl(
      String url, Collection<? extends ImageUrlExtensionProvider> extensionProviders) {
    try {
      String path = URI.create(url).getPath();
      if (path == null) return ".img";
      String lower = path.toLowerCase(Locale.ROOT);
      for (String extension : imageExtensions(extensionProviders)) {
        if (lower.endsWith(extension)) return extension;
      }
    } catch (Exception ignored) {
      // Fall back to a safe temporary-image suffix.
    }
    return ".img";
  }

  private List<String> extractUrls(
      String text,
      Collection<? extends ImageUrlExtensionProvider> extensionProviders,
      boolean directImages) {
    if (text == null || text.isBlank()) return List.of();

    Set<String> imageExtensions = imageExtensions(extensionProviders);
    Matcher matcher = URL_PATTERN.matcher(text);
    Set<String> out = new LinkedHashSet<>();
    while (matcher.find()) {
      String raw = matcher.group(1);
      UrlParts parts = splitUrlTrailingPunct(raw);
      String url = LinkPreviewFetchPreflightService.normalizeUrl(parts.url());
      boolean imageUrl = isLikelyDirectImageUrl(url, imageExtensions);
      if (directImages == imageUrl && isLikelyHttpUrl(url)) {
        out.add(url);
      }
    }
    return new ArrayList<>(out);
  }

  private static boolean isLikelyHttpUrl(String url) {
    if (url == null || url.isBlank()) return false;
    try {
      URI uri = URI.create(url);
      String scheme = uri.getScheme();
      if (scheme == null) return false;
      scheme = scheme.toLowerCase(Locale.ROOT);
      return scheme.equals("http") || scheme.equals("https");
    } catch (Exception ignored) {
      return false;
    }
  }

  private static boolean isLikelyDirectImageUrl(String url, Set<String> imageExtensions) {
    if (url == null || url.isBlank()) return false;
    try {
      URI uri = URI.create(url);
      String path = uri.getPath();
      if (path == null) return false;
      String lowerPath = path.toLowerCase(Locale.ROOT);
      for (String extension : imageExtensions) {
        if (lowerPath.endsWith(extension)) return true;
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
      if (c == '.' || c == ',' || c == ')' || c == ']' || c == '}' || c == '>' || c == '!'
          || c == '?' || c == ';' || c == ':' || c == '\'' || c == '"') {
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
