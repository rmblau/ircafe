package cafe.woden.ircclient.bouncer.spi;

/**
 * ServiceLoader-backed consumer of generic discovery events for one bouncer backend.
 *
 * <p>{@link #backendId()} is trimmed and matched case-insensitively by IRCafe. Providers should
 * return a stable, non-blank id that does not collide with another handler. When more than one
 * resolved handler claims the same normalized id, the first provider selected by IRCafe wins.
 *
 * <p>{@link #onNetworkDiscovered(BouncerDiscoveredNetwork)} receives the portable, normalized event
 * value. {@link #onOriginDisconnected(String)} is the matching lifecycle callback for clearing any
 * provider-owned state associated with the origin connection. IRCafe owns application registries,
 * runtime configuration, auto-connect scheduling, UI refreshes, and installed-plugin lifecycle.
 *
 * <p>External providers must be public, expose a public no-argument constructor, and register their
 * implementation in {@code
 * META-INF/services/cafe.woden.ircclient.bouncer.spi.BouncerBackendDiscoveryHandler}.
 */
public interface BouncerBackendDiscoveryHandler {

  /** Returns the stable backend id used to route discovery and disconnect events. */
  String backendId();

  /** Handles one normalized discovery event for this backend. */
  void onNetworkDiscovered(BouncerDiscoveredNetwork network);

  /** Handles disconnection of an origin server previously used by this backend. */
  void onOriginDisconnected(String originServerId);
}
