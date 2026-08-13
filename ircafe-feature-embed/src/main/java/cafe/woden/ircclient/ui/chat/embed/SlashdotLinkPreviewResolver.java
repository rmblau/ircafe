package cafe.woden.ircclient.ui.chat.embed;

import cafe.woden.ircclient.ui.chat.embed.spi.BuiltInLinkPreviewResolverOrders;
import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreview;
import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreviewHttp;
import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreviewResolver;
import java.io.ByteArrayInputStream;
import java.net.URI;

/** Best-effort resolver for Slashdot story pages with a longer excerpt than OG/meta provides. */
final class SlashdotLinkPreviewResolver implements LinkPreviewResolver {

  private final int maxHtmlBytes;

  SlashdotLinkPreviewResolver(int maxHtmlBytes) {
    this.maxHtmlBytes = maxHtmlBytes;
  }

  @Override
  public int sortOrder() {
    return BuiltInLinkPreviewResolverOrders.SLASHDOT;
  }

  @Override
  public LinkPreview tryResolve(URI uri, String originalUrl, LinkPreviewHttp http)
      throws Exception {
    if (!SlashdotPreviewUtil.isSlashdotStoryUri(uri)) return null;

    var resp =
        http.getStream(
            uri,
            "text/html,application/xhtml+xml;q=0.9,*/*;q=0.8",
            LinkPreviewHttpDefaults.headers(
                LinkPreviewHttpDefaults.HEADER_USER_AGENT,
                LinkPreviewHttpDefaults.BROWSER_USER_AGENT,
                LinkPreviewHttpDefaults.HEADER_ACCEPT_LANGUAGE,
                LinkPreviewHttpDefaults.ACCEPT_LANGUAGE));

    int status = resp.statusCode();
    if (status < 200 || status >= 300) {
      return null; // fall back to OG parser
    }

    String ct = resp.headers().firstValue("content-type").orElse("");
    if (!LinkPreviewHtmlSupport.looksLikeHtml(ct)) {
      return null;
    }

    byte[] bytes = LinkPreviewHtmlSupport.readUpToBytes(resp.body(), maxHtmlBytes);
    if (bytes.length == 0) return null;

    var doc = org.jsoup.Jsoup.parse(new ByteArrayInputStream(bytes), null, originalUrl);

    LinkPreview base = LinkPreviewParser.parse(doc, originalUrl);

    String title = base.title();
    if (title != null) {
      title = title.replace(" - Slashdot", "").strip();
    }

    SlashdotPreviewUtil.StoryParts parts =
        SlashdotPreviewUtil.extractStoryParts(doc, title, base.description());
    String desc = parts.summary();
    if ((parts.submitter() != null && !parts.submitter().isBlank())
        || (parts.date() != null && !parts.date().isBlank())) {
      StringBuilder sb = new StringBuilder();
      if (parts.submitter() != null && !parts.submitter().isBlank()) {
        sb.append("Submitter: ").append(parts.submitter()).append("\n");
      }
      if (parts.date() != null && !parts.date().isBlank()) {
        sb.append("Date: ").append(parts.date()).append("\n");
      }
      sb.append("\n");
      if (desc != null) sb.append(desc);
      desc = sb.toString();
    }
    String site = "Slashdot";

    return new LinkPreview(base.url(), title, desc, site, base.imageUrl(), base.mediaCount());
  }
}
