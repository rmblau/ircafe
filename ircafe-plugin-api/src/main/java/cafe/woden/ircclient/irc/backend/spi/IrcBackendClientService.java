package cafe.woden.ircclient.irc.backend.spi;

/**
 * Portable identity contract for backend-specific IRC transport providers.
 *
 * <p>The full runtime transport surface remains app-owned because it exposes network lifecycle,
 * RxJava streams, and backend control ports. Plugin-facing code should use this contract only for
 * backend identity and best-effort lifecycle nudges.
 */
public interface IrcBackendClientService {

  default String backendId() {
    return "";
  }

  /** Re-schedule heartbeats for active connections (best effort). */
  default void rescheduleActiveHeartbeats() {}
}
