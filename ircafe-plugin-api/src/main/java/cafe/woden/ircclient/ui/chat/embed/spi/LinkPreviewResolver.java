package cafe.woden.ircclient.ui.chat.embed.spi;

import java.net.URI;

/**
 * Strategy interface for resolving a {@link LinkPreview} for a URL.
 *
 * <p>Resolvers should return {@code null} when they don't apply to the URL. They may throw when
 * they apply but fail in a meaningful way.
 *
 * <p>Plugins register implementations in {@code
 * META-INF/services/cafe.woden.ircclient.ui.chat.embed.spi.LinkPreviewResolver}.
 */
public interface LinkPreviewResolver {

  LinkPreview tryResolve(URI uri, String originalUrl, LinkPreviewHttp http) throws Exception;
}
