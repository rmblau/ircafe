package cafe.woden.ircclient.ui.chat.embed;

import cafe.woden.ircclient.ui.chat.embed.spi.BuiltInLinkPreviewResolverOrders;
import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreview;
import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreviewHttp;
import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreviewResolver;
import java.io.ByteArrayInputStream;
import java.net.URI;

final class ImgurLinkPreviewResolver implements LinkPreviewResolver {

  private final int maxHtmlBytes;

  ImgurLinkPreviewResolver(int maxHtmlBytes) {
    this.maxHtmlBytes = maxHtmlBytes;
  }

  @Override
  public int sortOrder() {
    return BuiltInLinkPreviewResolverOrders.IMGUR;
  }

  @Override
  public LinkPreview tryResolve(URI uri, String originalUrl, LinkPreviewHttp http)
      throws Exception {
    if (!ImgurPreviewUtil.isImgurUri(uri)) {
      return null;
    }

    var resp =
        http.getStream(
            uri,
            "text/html,application/xhtml+xml;q=0.9,*/*;q=0.8",
            LinkPreviewHttpDefaults.headers(
                LinkPreviewHttpDefaults.HEADER_USER_AGENT,
                LinkPreviewHttpDefaults.BROWSER_USER_AGENT,
                LinkPreviewHttpDefaults.HEADER_ACCEPT_LANGUAGE,
                LinkPreviewHttpDefaults.ACCEPT_LANGUAGE,
                LinkPreviewHttpDefaults.HEADER_REFERER,
                "https://imgur.com/"));

    int status = resp.statusCode();
    if (status < 200 || status >= 300) {
      return null;
    }

    String ct = resp.headers().firstValue("content-type").orElse("");
    if (!LinkPreviewHtmlSupport.looksLikeHtml(ct)) {
      return null;
    }

    byte[] bytes = LinkPreviewHtmlSupport.readUpToBytes(resp.body(), maxHtmlBytes);
    if (bytes.length == 0) return null;

    var doc = org.jsoup.Jsoup.parse(new ByteArrayInputStream(bytes), null, originalUrl);
    return ImgurPreviewUtil.parsePostDocument(doc, originalUrl);
  }
}
