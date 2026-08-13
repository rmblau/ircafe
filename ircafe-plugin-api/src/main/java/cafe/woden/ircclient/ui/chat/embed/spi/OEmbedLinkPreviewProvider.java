package cafe.woden.ircclient.ui.chat.embed.spi;

import java.net.URI;

/**
 * ServiceLoader contribution for sites handled by IRCafe's generic oEmbed resolver.
 *
 * <p>Providers are evaluated in catalog order. The first provider whose {@link #matches(URI)}
 * method returns {@code true} owns that oEmbed attempt. Return {@code false} for unrelated URLs and
 * return {@code null} from {@link #endpointFor(URI, String)} when no request should be made. IRCafe
 * owns HTTP transport, response parsing, proxy and timeout policy, and final preview construction.
 *
 * <p>Provider classes are deduplicated with built-in or earlier providers winning. Register
 * implementations with {@code
 * META-INF/services/cafe.woden.ircclient.ui.chat.embed.spi.OEmbedLinkPreviewProvider}.
 * Implementations must be public and expose a public no-argument constructor.
 */
public interface OEmbedLinkPreviewProvider {

  /**
   * Returns the stable lowercase provider id used for provider-specific behavior.
   *
   * @return stable provider id
   */
  String id();

  /**
   * Returns whether this provider owns the target URL.
   *
   * @param uri normalized target URI
   * @return {@code true} when this provider should handle the URL
   */
  boolean matches(URI uri);

  /**
   * Builds the oEmbed endpoint, or returns {@code null} when no request should be made.
   *
   * @param uri normalized target URI
   * @param originalUrl normalized URL string retained by IRCafe
   * @return oEmbed endpoint, or {@code null}
   */
  URI endpointFor(URI uri, String originalUrl);

  /**
   * Returns the fallback site name used when the response omits {@code provider_name}.
   *
   * @return fallback site name, or {@code null}
   */
  String defaultSiteName();

  /**
   * Returns the fallback title used when the response omits {@code title}.
   *
   * @param fields normalized response fields
   * @return fallback title, or {@code null}
   */
  String titleFallback(OEmbedResponseFields fields);
}
