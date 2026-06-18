package cafe.woden.ircclient.ui.chat.embed.builtins;

import cafe.woden.ircclient.ui.chat.embed.spi.OEmbedLinkPreviewProvider;
import cafe.woden.ircclient.ui.chat.embed.spi.OEmbedResponseFields;
import com.google.auto.service.AutoService;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/** Built-in oEmbed provider for Spotify URLs. */
@AutoService(OEmbedLinkPreviewProvider.class)
public final class BuiltInSpotifyOEmbedLinkPreviewProvider implements OEmbedLinkPreviewProvider {

  @Override
  public String id() {
    return "spotify";
  }

  @Override
  public boolean matches(URI uri) {
    String host = uri == null ? null : uri.getHost();
    if (host == null || host.isBlank()) return false;
    String h = host.toLowerCase(Locale.ROOT);
    return h.equals("open.spotify.com") || h.equals("spotify.link") || h.endsWith(".spotify.com");
  }

  @Override
  public URI endpointFor(URI uri, String originalUrl) {
    String enc = URLEncoder.encode(originalUrl, StandardCharsets.UTF_8);
    // Spotify's public oEmbed endpoint lives on open.spotify.com.
    return URI.create("https://open.spotify.com/oembed?url=" + enc);
  }

  @Override
  public String defaultSiteName() {
    return "Spotify";
  }

  @Override
  public String titleFallback(OEmbedResponseFields fields) {
    return "Spotify";
  }
}
