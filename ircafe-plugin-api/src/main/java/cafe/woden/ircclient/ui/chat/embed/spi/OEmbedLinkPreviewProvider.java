package cafe.woden.ircclient.ui.chat.embed.spi;

import java.net.URI;

/**
 * ServiceLoader-backed contribution point for URL providers handled by the generic oEmbed preview
 * resolver.
 *
 * <p>Register implementations with {@code
 * META-INF/services/cafe.woden.ircclient.ui.chat.embed.spi.OEmbedLinkPreviewProvider}.
 */
public interface OEmbedLinkPreviewProvider {

  /** Stable provider id used for provider-specific preview behavior. */
  String id();

  /** Whether this provider should attempt to resolve the target URL. */
  boolean matches(URI uri);

  /** Builds the oEmbed endpoint for the target URL, or returns {@code null} when unavailable. */
  URI endpointFor(URI uri, String originalUrl);

  /** Fallback site name used when the oEmbed response omits {@code provider_name}. */
  String defaultSiteName();

  /** Fallback title used when the oEmbed response omits {@code title}. */
  String titleFallback(OEmbedResponseFields fields);
}
