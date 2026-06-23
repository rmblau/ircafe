package cafe.woden.ircclient.ui.chat.embed;

import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreview;
import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreviewHttp;
import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreviewResolver;
import cafe.woden.ircclient.ui.chat.embed.spi.OEmbedLinkPreviewProvider;
import cafe.woden.ircclient.ui.chat.embed.spi.OEmbedResponseFields;
import java.net.URI;
import java.util.List;

/**
 * Generic oEmbed-based link preview resolver.
 *
 * <p>This consolidates multiple "provider" resolvers (Spotify, Mastodon, etc.) that all follow the
 * same pattern: detect URL, call an oEmbed endpoint, map the response into a {@link LinkPreview}.
 */
final class OEmbedLinkPreviewResolver implements LinkPreviewResolver {

  private final List<OEmbedLinkPreviewProvider> providers;

  OEmbedLinkPreviewResolver(List<OEmbedLinkPreviewProvider> providers) {
    this.providers = providers == null ? List.of() : List.copyOf(providers);
  }

  @Override
  public LinkPreview tryResolve(URI uri, String originalUrl, LinkPreviewHttp http) {
    try {
      if (uri == null || originalUrl == null || http == null) return null;

      OEmbedLinkPreviewProvider p = firstMatchingProvider(uri);
      if (p == null) return null;

      URI api = p.endpointFor(uri, originalUrl);
      if (api == null) return null;

      // Some providers (including Mastodon) advertise oEmbed as application/json+oembed.
      var resp = http.getString(api, "application/json+oembed,application/json", null);
      if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
        return null;
      }

      String json = resp.body();
      if (json == null || json.isBlank()) return null;

      OEmbedResponseFields fields =
          new OEmbedResponseFields(
              stripToNull(TinyJson.findString(json, "title")),
              stripToNull(TinyJson.findString(json, "author_name")),
              stripToNull(TinyJson.findString(json, "provider_name")),
              stripToNull(TinyJson.findString(json, "thumbnail_url")),
              stripToNull(TinyJson.findString(json, "html")));

      String siteName = firstNonBlank(fields.providerName(), stripToNull(p.defaultSiteName()));
      String title = firstNonBlank(fields.title(), stripToNull(p.titleFallback(fields)), siteName);

      // Keep descriptions short and stable.
      String description = null;

      // Mastodon: the oEmbed title is often generic; the HTML field contains the actual post.
      if ("mastodon".equals(p.id()) && fields.html() != null) {
        description = mastodonDescFromOEmbedHtml(fields.html());
      }

      // Generic fallback: use author_name.
      if ((description == null || description.isBlank()) && fields.authorName() != null) {
        description = "by " + fields.authorName();
      }

      if (title == null) return null;
      int mediaCount = (fields.thumbnailUrl() != null && !fields.thumbnailUrl().isBlank()) ? 1 : 0;
      return new LinkPreview(
          originalUrl, title, description, siteName, fields.thumbnailUrl(), mediaCount);
    } catch (Exception ignored) {
      return null;
    }
  }

  private OEmbedLinkPreviewProvider firstMatchingProvider(URI uri) {
    for (OEmbedLinkPreviewProvider p : providers) {
      try {
        if (p.matches(uri)) return p;
      } catch (Exception ignored) {
      }
    }
    return null;
  }

  private static String mastodonDescFromOEmbedHtml(String html) {
    try {
      if (html == null || html.isBlank()) return null;
      org.jsoup.nodes.Document d = org.jsoup.Jsoup.parseBodyFragment(html);
      // Avoid script placeholder text.
      d.select("script,style").remove();

      // Prefer paragraphs inside the blockquote (post content), which tends to be cleaner.
      var ps = d.select("blockquote p");
      String text;
      if (ps != null && !ps.isEmpty()) {
        StringBuilder sb = new StringBuilder();
        for (org.jsoup.nodes.Element p : ps) {
          String t = p.text();
          if (t == null || t.isBlank()) continue;
          if (sb.length() > 0) sb.append("\n");
          sb.append(t.strip());
        }
        text = sb.toString();
      } else {
        var bq = d.selectFirst("blockquote");
        text = bq != null ? bq.text() : d.text();
      }

      text = stripToNull(text);
      if (text == null) return null;
      // Keep it short; UI will also clamp, but this prevents huge tooltips and layout thrash.
      int max = 320;
      if (text.length() > max) {
        text = text.substring(0, max).strip() + "…";
      }
      return text;
    } catch (Exception ignored) {
      return null;
    }
  }

  // ---- helpers ----

  private static String stripToNull(String s) {
    if (s == null) return null;
    String v = s.strip();
    return v.isEmpty() ? null : v;
  }

  private static String firstNonBlank(String... values) {
    if (values == null) return null;
    for (String v : values) {
      if (v != null && !v.isBlank()) return v.strip();
    }
    return null;
  }
}
