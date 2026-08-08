package cafe.woden.ircclient.ui.chat.embed.spi;

import java.net.URI;
import java.util.Map;

/**
 * ServiceLoader contribution for embed HTTP request headers.
 *
 * <p>The provider is used for both direct-image and link-preview fetches. Return an empty map for
 * unrelated URIs. IRCafe trims names and values, ignores blank entries, applies providers in order,
 * and lets later valid values replace earlier values with the same header name. Provider failures
 * are isolated from the fetch and may be reported through plugin diagnostics.
 *
 * <p>Register implementations with {@code
 * META-INF/services/cafe.woden.ircclient.ui.chat.embed.spi.EmbedHttpHeaderProvider}.
 * Implementations must be public and expose a public no-argument constructor.
 */
public interface EmbedHttpHeaderProvider {

  /**
   * Returns extra headers for the URI, or an empty map when this provider does not apply.
   *
   * @param uri target embed URI
   * @return contributed headers, or an empty map
   */
  Map<String, String> embedHttpHeaders(URI uri);
}
