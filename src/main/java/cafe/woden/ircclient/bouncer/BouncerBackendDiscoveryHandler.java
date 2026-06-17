package cafe.woden.ircclient.bouncer;

/**
 * Legacy bouncer backend discovery handler service name.
 *
 * @deprecated register {@link cafe.woden.ircclient.bouncer.spi.BouncerBackendDiscoveryHandler}
 *     implementations under {@code
 *     META-INF/services/cafe.woden.ircclient.bouncer.spi.BouncerBackendDiscoveryHandler}.
 */
@Deprecated(since = "0.1", forRemoval = false)
public interface BouncerBackendDiscoveryHandler
    extends cafe.woden.ircclient.bouncer.spi.BouncerBackendDiscoveryHandler {}
