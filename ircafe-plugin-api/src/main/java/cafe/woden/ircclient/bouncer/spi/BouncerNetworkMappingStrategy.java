package cafe.woden.ircclient.bouncer.spi;

import java.util.List;
import java.util.Set;

/**
 * ServiceLoader-backed mapping policy from discovered bouncer networks to portable ephemeral-server
 * values.
 *
 * <p>{@link #backendId()} is trimmed and matched case-insensitively by IRCafe. Providers should
 * return a stable, non-blank id. Metadata methods describe the backend in IRCafe's registry; their
 * returned values are normalized by the app-side feature catalog.
 *
 * <p>New providers should normally implement the context-aware {@link
 * #resolveNetwork(BouncerServerProfile, BouncerDiscoveredNetwork, BouncerNetworkMappingContext)}
 * overload. The two-argument overload remains available for older stateless providers. IRCafe then
 * calls {@link #buildEphemeralServer(BouncerServerProfile, ResolvedBouncerNetwork, List)} with the
 * app-resolved auto-join channels. Providers return portable values only; IRCafe retains registry
 * mutation, persistence, connection scheduling, disconnect cleanup, and UI ownership.
 *
 * <p>External providers must be public, expose a public no-argument constructor, and register their
 * implementation in {@code
 * META-INF/services/cafe.woden.ircclient.bouncer.spi.BouncerNetworkMappingStrategy}.
 */
public interface BouncerNetworkMappingStrategy {

  /** Returns the stable backend id represented by this strategy. */
  String backendId();

  /** Returns the prefix used for deterministic ephemeral server ids. */
  default String ephemeralIdPrefix() {
    return backendId() + ":";
  }

  /** Returns the user-facing label for this backend's discovered-network group. */
  default String networksGroupLabel() {
    return backendId() + " Networks";
  }

  /** Returns normalized capability hints associated with this backend. */
  default Set<String> capabilityHints() {
    return Set.of();
  }

  /**
   * Resolves a discovered network without app-provided runtime policy.
   *
   * <p>Older stateless providers may override this method. New providers should prefer the
   * context-aware overload.
   */
  default ResolvedBouncerNetwork resolveNetwork(
      BouncerServerProfile bouncer, BouncerDiscoveredNetwork network) {
    throw new UnsupportedOperationException(
        "Bouncer mapping strategies must implement resolveNetwork(bouncer, network) "
            + "or resolveNetwork(bouncer, network, context).");
  }

  /**
   * Resolves one discovered network using the portable runtime policy snapshot supplied by IRCafe.
   */
  default ResolvedBouncerNetwork resolveNetwork(
      BouncerServerProfile bouncer,
      BouncerDiscoveredNetwork network,
      BouncerNetworkMappingContext context) {
    return resolveNetwork(bouncer, network);
  }

  /**
   * Builds the portable ephemeral-server specification after IRCafe resolves auto-join channels.
   */
  default BouncerEphemeralServerSpec buildEphemeralServer(
      BouncerServerProfile bouncer,
      ResolvedBouncerNetwork resolved,
      List<String> autoJoinChannels) {
    return BouncerEphemeralServerSpec.from(resolved, autoJoinChannels);
  }

  /** Returns an optional provider-specific identifier used only in debug logging. */
  default String networkDebugId(BouncerDiscoveredNetwork network) {
    return "";
  }
}
