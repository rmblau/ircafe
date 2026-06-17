package cafe.woden.ircclient.bouncer;

/**
 * Legacy bouncer network mapping strategy service name.
 *
 * @deprecated register {@link cafe.woden.ircclient.bouncer.spi.BouncerNetworkMappingStrategy}
 *     implementations under {@code
 *     META-INF/services/cafe.woden.ircclient.bouncer.spi.BouncerNetworkMappingStrategy}.
 */
@Deprecated(since = "0.1", forRemoval = false)
public interface BouncerNetworkMappingStrategy
    extends cafe.woden.ircclient.bouncer.spi.BouncerNetworkMappingStrategy {}
