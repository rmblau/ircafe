package cafe.woden.ircclient.ui.chat.embed.spi;

import java.net.URI;
import java.util.Map;

/**
 * ServiceLoader-backed contribution point for embed HTTP request headers.
 *
 * <p>This is the preferred HTTP-header SPI for new plugins. It is used for both direct image
 * fetches and link-preview fetches, so plugins can keep host-specific header behavior in one
 * provider instead of implementing separate image and preview adapters.
 *
 * <p>Register implementations with {@code
 * META-INF/services/cafe.woden.ircclient.ui.chat.embed.spi.EmbedHttpHeaderProvider}.
 */
public interface EmbedHttpHeaderProvider {

  /**
   * Returns extra HTTP headers to apply when fetching the given embed URI.
   *
   * <p>Providers should return an empty map when they do not apply to the URI. Blank header names
   * or values are ignored by IRCafe before the request is sent.
   */
  Map<String, String> embedHttpHeaders(URI uri);
}
