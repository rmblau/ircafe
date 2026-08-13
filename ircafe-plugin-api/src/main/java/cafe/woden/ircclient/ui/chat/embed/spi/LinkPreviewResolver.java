package cafe.woden.ircclient.ui.chat.embed.spi;

import java.net.URI;

/**
 * ServiceLoader strategy for resolving a complete {@link LinkPreview}.
 *
 * <p>Resolvers are invoked in ascending {@link #sortOrder()} after IRCafe has normalized and
 * preflighted the URL. Return {@code null} when the resolver does not own the URL or cannot produce
 * a preview so later resolvers can continue. Exceptions are isolated by IRCafe, associated with the
 * installed plugin in diagnostics, and do not stop the remaining resolver chain.
 *
 * <p>Use the supplied {@link LinkPreviewHttp} facade for network requests. IRCafe owns proxy
 * configuration, shared embed headers, request timeouts, transport lifecycle, caching, and
 * diagnostics. Providers must not retain the facade or request values after the call returns.
 *
 * <p>Provider classes are deduplicated with built-in or earlier providers winning. Plugins register
 * implementations in {@code
 * META-INF/services/cafe.woden.ircclient.ui.chat.embed.spi.LinkPreviewResolver}. Implementations
 * must be public and expose a public no-argument constructor.
 */
public interface LinkPreviewResolver {

  /**
   * Returns this resolver's ascending execution order.
   *
   * <p>The default places external resolvers after IRCafe's built-in generic fallbacks. Override it
   * only when the resolver deliberately needs a different position.
   *
   * @return ascending resolver execution order
   */
  default int sortOrder() {
    return BuiltInLinkPreviewResolverOrders.PLUGIN_DEFAULT;
  }

  /**
   * Attempts to resolve a preview, returning {@code null} to continue the resolver chain.
   *
   * @param uri normalized target URI
   * @param originalUrl normalized URL string retained for the resulting preview
   * @param http app-owned HTTP facade for preview requests
   * @return resolved preview, or {@code null} to continue the resolver chain
   * @throws Exception when the resolver owns the request but resolution fails
   */
  LinkPreview tryResolve(URI uri, String originalUrl, LinkPreviewHttp http) throws Exception;
}
