package cafe.woden.ircclient.ui.chat.embed;

import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreview;
import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreviewHttp;
import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreviewResolver;
import com.google.auto.service.AutoService;
import java.net.URI;
import java.time.Duration;

@AutoService(LinkPreviewResolver.class)
public final class YouTubeLinkPreviewResolver implements LinkPreviewResolver {

  @Override
  public LinkPreview tryResolve(URI uri, String originalUrl, LinkPreviewHttp http) {
    try {
      if (!YouTubePreviewUtil.isYouTubeVideoUri(uri)) return null;

      // Canonicalize so open/copy feels consistent.
      String id = YouTubePreviewUtil.extractVideoId(uri);
      URI canonical = id != null ? YouTubePreviewUtil.canonicalWatchUri(id) : null;
      URI target = canonical != null ? canonical : uri;

      URI api = YouTubePreviewUtil.oEmbedUri(target);
      if (api == null) return null;

      var resp = http.getString(api, "application/json", null);
      if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
        return null;
      }

      // Enrich with yt-dlp metadata (duration/views/likes/description) if available.
      // This avoids scraping the YouTube watch page, which is often consent-gated.
      var meta = YouTubePreviewUtil.tryFetchYtDlpMeta(target.toString(), Duration.ofSeconds(6));

      return YouTubePreviewUtil.parseOEmbedJson(resp.body(), target, null, meta);
    } catch (Exception ignored) {
      return null;
    }
  }
}
