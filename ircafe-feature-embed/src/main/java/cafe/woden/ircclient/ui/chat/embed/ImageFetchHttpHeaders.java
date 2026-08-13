package cafe.woden.ircclient.ui.chat.embed;

import cafe.woden.ircclient.ui.chat.embed.spi.EmbedHttpHeaderProvider;
import java.net.URI;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.jmolecules.architecture.layered.InterfaceLayer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/** Builds effective HTTP headers for root-owned image fetch adapters. */
@Component
@InterfaceLayer
@Lazy
public class ImageFetchHttpHeaders {

  private static final String IMAGE_ACCEPT =
      "image/jpeg,image/png,image/webp,image/gif,image/*;q=0.5,*/*;q=0.4";

  private final LinkPreviewHttpHeaderCatalog headerCatalog;

  public ImageFetchHttpHeaders() {
    this(new LinkPreviewHttpHeaderCatalog());
  }

  @Autowired
  public ImageFetchHttpHeaders(LinkPreviewHttpHeaderCatalog headerCatalog) {
    this.headerCatalog = headerCatalog != null ? headerCatalog : new LinkPreviewHttpHeaderCatalog();
  }

  public LinkPreviewHttpHeaderResult headersFor(
      URI uri, Collection<? extends EmbedHttpHeaderProvider> headerProviders) {
    LinkedHashMap<String, String> baseHeaders = new LinkedHashMap<>();
    baseHeaders.put(
        LinkPreviewHttpDefaults.HEADER_USER_AGENT,
        needsInstagramReferer(uri)
            ? LinkPreviewHttpDefaults.BROWSER_USER_AGENT
            : LinkPreviewHttpDefaults.USER_AGENT);
    baseHeaders.put(
        LinkPreviewHttpDefaults.HEADER_ACCEPT_LANGUAGE, LinkPreviewHttpDefaults.ACCEPT_LANGUAGE);
    baseHeaders.put(LinkPreviewHttpDefaults.HEADER_ACCEPT_ENCODING, "gzip");
    // IMPORTANT: Do NOT advertise AVIF by default.
    // Some CDNs will pick AVIF whenever it's present in Accept (ignoring q=), and ImageIO
    // can't decode it without a native/plugin decoder. If AVIF support is added later,
    // image/avif can be added back here.
    baseHeaders.put(LinkPreviewHttpDefaults.HEADER_ACCEPT, IMAGE_ACCEPT);
    // Some CDNs are picky; these headers help us look like a browser fetching an image.
    baseHeaders.put("Sec-Fetch-Dest", "image");
    baseHeaders.put("Sec-Fetch-Mode", "no-cors");

    // Some IMDb/Amazon image endpoints can be picky without a referer.
    if (needsImdbReferer(uri)) {
      baseHeaders.put(LinkPreviewHttpDefaults.HEADER_REFERER, "https://www.imdb.com/");
    }

    // Instagram CDN endpoints can return bot-check HTML without a referer.
    if (!baseHeaders.containsKey(LinkPreviewHttpDefaults.HEADER_REFERER)
        && needsInstagramReferer(uri)) {
      baseHeaders.put(LinkPreviewHttpDefaults.HEADER_REFERER, "https://www.instagram.com/");
    }

    LinkPreviewHttpHeaderResult providerResult =
        headerCatalog.applyProviderHeaders(baseHeaders, uri, headerProviders);
    List<LinkPreviewHttpHeaderProviderFailure> failures =
        providerResult.failures() == null ? List.of() : providerResult.failures();
    return new LinkPreviewHttpHeaderResult(Map.copyOf(providerResult.headers()), failures);
  }

  private static boolean needsImdbReferer(URI uri) {
    if (uri == null) return false;
    String host = uri.getHost();
    if (host == null) return false;
    host = host.toLowerCase(Locale.ROOT);
    return host.contains("media-amazon.com")
        || host.contains("images-amazon.com")
        || host.contains("amazonaws.com");
  }

  private static boolean needsInstagramReferer(URI uri) {
    if (uri == null) return false;
    String host = uri.getHost();
    if (host == null || host.isBlank()) return false;
    String h = host.toLowerCase(Locale.ROOT);
    if (h.startsWith("www.")) h = h.substring(4);

    if (h.equals("instagram.com") || h.endsWith(".instagram.com") || h.equals("instagr.am")) {
      return true;
    }

    // Common direct media hosts.
    if (h.contains("cdninstagram.com")) return true;
    if (h.endsWith("fbcdn.net") && h.contains("instagram")) return true;
    return false;
  }
}
