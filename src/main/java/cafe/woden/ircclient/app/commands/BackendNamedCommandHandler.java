package cafe.woden.ircclient.app.commands;

/**
 * Legacy backend-named command parser service name.
 *
 * @deprecated register {@link cafe.woden.ircclient.app.commands.spi.BackendNamedCommandHandler}
 *     implementations under {@code
 *     META-INF/services/cafe.woden.ircclient.app.commands.spi.BackendNamedCommandHandler}.
 */
@Deprecated(since = "0.1", forRemoval = false)
public interface BackendNamedCommandHandler
    extends cafe.woden.ircclient.app.commands.spi.BackendNamedCommandHandler {}
