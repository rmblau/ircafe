package cafe.woden.ircclient.ui.chat.embed;

import cafe.woden.ircclient.ui.chat.embed.spi.NewsPublisherProfile;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.List;
import java.util.Objects;

/** Generic resolver for article-style news pages with richer metadata and summary extraction. */
final class NewsLinkPreviewResolver implements LinkPreviewResolver {

  private final int maxHtmlBytes;
  private final List<NewsPublisherProfile> publisherProfiles;

  NewsLinkPreviewResolver(int maxHtmlBytes) {
    this(maxHtmlBytes, NewsPreviewUtil.defaultPublisherProfiles());
  }

  NewsLinkPreviewResolver(int maxHtmlBytes, List<NewsPublisherProfile> publisherProfiles) {
    this.maxHtmlBytes = maxHtmlBytes;
    this.publisherProfiles =
        List.copyOf(
            Objects.requireNonNullElse(
                publisherProfiles, NewsPreviewUtil.defaultPublisherProfiles()));
  }

  @Override
  public LinkPreview tryResolve(URI uri, String originalUrl, PreviewHttp http) throws Exception {
    if (!NewsPreviewUtil.isLikelyNewsArticleUri(uri, publisherProfiles)) {
      return null;
    }

    var resp =
        http.getStream(
            uri,
            "text/html,application/xhtml+xml;q=0.9,*/*;q=0.8",
            PreviewHttp.headers(
                PreviewHttp.HEADER_USER_AGENT, PreviewHttp.BROWSER_USER_AGENT,
                PreviewHttp.HEADER_ACCEPT_LANGUAGE, PreviewHttp.ACCEPT_LANGUAGE));

    int status = resp.statusCode();
    if (status < 200 || status >= 300) {
      return null;
    }

    String ct = resp.headers().firstValue("content-type").orElse("");
    if (!PreviewHttp.looksLikeHtml(ct)) {
      return null;
    }

    byte[] bytes = PreviewHttp.readUpToBytes(resp.body(), maxHtmlBytes);
    if (bytes.length == 0) return null;

    var doc = org.jsoup.Jsoup.parse(new ByteArrayInputStream(bytes), null, originalUrl);
    return NewsPreviewUtil.parseArticleDocument(doc, originalUrl, publisherProfiles);
  }
}
