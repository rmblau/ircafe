package cafe.woden.ircclient.ui.chat.embed.builtins;

import cafe.woden.ircclient.ui.chat.embed.spi.OEmbedLinkPreviewProvider;
import cafe.woden.ircclient.ui.chat.embed.spi.OEmbedResponseFields;
import com.google.auto.service.AutoService;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/** Built-in oEmbed provider for Mastodon status URLs. */
@AutoService(OEmbedLinkPreviewProvider.class)
public final class BuiltInMastodonOEmbedLinkPreviewProvider implements OEmbedLinkPreviewProvider {
  private static final Pattern MASTODON_AT_STYLE = Pattern.compile("^/@[^/]+/\\d+(/.*)?$");
  private static final Pattern MASTODON_USERS_STYLE =
      Pattern.compile("^/users/[^/]+/statuses/\\d+(/.*)?$");
  private static final Pattern MASTODON_WEB_STYLE = Pattern.compile("^/web/statuses/\\d+(/.*)?$");

  @Override
  public String id() {
    return "mastodon";
  }

  @Override
  public boolean matches(URI uri) {
    String host = uri == null ? null : uri.getHost();
    if (host == null || host.isBlank()) return false;
    String path = uri.getPath() == null ? "" : uri.getPath();
    return looksLikeMastodonStatusPath(path);
  }

  @Override
  public URI endpointFor(URI uri, String originalUrl) {
    // Use the instance that served the URL (scheme + authority).
    if (uri == null || uri.getScheme() == null || uri.getAuthority() == null) return null;
    String base = uri.getScheme() + "://" + uri.getAuthority();
    String enc = URLEncoder.encode(originalUrl, StandardCharsets.UTF_8);
    // Some deployments expect an explicit format.
    return URI.create(base + "/api/oembed?format=json&url=" + enc);
  }

  @Override
  public String defaultSiteName() {
    // provider_name may be the instance name; we keep it when present.
    return "Mastodon";
  }

  @Override
  public String titleFallback(OEmbedResponseFields fields) {
    if (fields != null && fields.authorName() != null) return "Post by " + fields.authorName();
    return "Mastodon post";
  }

  private static boolean looksLikeMastodonStatusPath(String path) {
    if (path == null) return false;
    String p = path.strip();
    if (p.isEmpty()) return false;
    while (p.endsWith("/")) p = p.substring(0, p.length() - 1);
    return MASTODON_AT_STYLE.matcher(p).matches()
        || MASTODON_USERS_STYLE.matcher(p).matches()
        || MASTODON_WEB_STYLE.matcher(p).matches();
  }
}
