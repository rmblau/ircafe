package cafe.woden.ircclient.app.commands.spi;

import java.util.Set;

/**
 * ServiceLoader-backed execution contribution for backend-scoped named commands.
 *
 * <p>Plugins register implementations in {@code
 * META-INF/services/cafe.woden.ircclient.app.commands.spi.BackendNamedCommandExecutor}.
 */
public interface BackendNamedCommandExecutor {

  Set<String> handledCommandNames();

  boolean handle(BackendNamedCommandExecutionContext context, BackendNamedCommandRequest command);
}
