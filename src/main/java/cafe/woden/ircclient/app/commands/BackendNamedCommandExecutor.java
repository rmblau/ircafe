package cafe.woden.ircclient.app.commands;

/**
 * Legacy backend-named command executor service name.
 *
 * @deprecated register {@link cafe.woden.ircclient.app.commands.spi.BackendNamedCommandExecutor}
 *     implementations under {@code
 *     META-INF/services/cafe.woden.ircclient.app.commands.spi.BackendNamedCommandExecutor}.
 */
@Deprecated(since = "0.1", forRemoval = false)
public interface BackendNamedCommandExecutor
    extends cafe.woden.ircclient.app.commands.spi.BackendNamedCommandExecutor {}
