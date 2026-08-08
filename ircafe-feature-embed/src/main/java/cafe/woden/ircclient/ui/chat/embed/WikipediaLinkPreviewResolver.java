package cafe.woden.ircclient.ui.chat.embed;

import cafe.woden.ircclient.ui.chat.embed.spi.BuiltInLinkPreviewResolverOrders;
import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreview;
import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreviewHttp;
import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreviewResolver;
import java.net.URI;

final class WikipediaLinkPreviewResolver implements LinkPreviewResolver {

  @Override
  public int sortOrder() {
    return BuiltInLinkPreviewResolverOrders.WIKIPEDIA;
  }

  @Override
  public LinkPreview tryResolve(URI uri, String originalUrl, LinkPreviewHttp http) {
    try {
      if (!WikipediaPreviewUtil.isWikipediaArticleUri(uri)) return null;
      URI api = WikipediaPreviewUtil.toSummaryApiUri(uri);
      if (api == null) return null;

      var resp = http.getString(api, "application/json", null);
      if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
        return null;
      }
      return WikipediaPreviewUtil.parseSummaryJson(resp.body(), uri);
    } catch (Exception ignored) {
      return null;
    }
  }
}
