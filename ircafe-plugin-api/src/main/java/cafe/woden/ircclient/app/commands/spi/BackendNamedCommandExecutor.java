package cafe.woden.ircclient.app.commands.spi;

import java.util.Set;

/**
 * ServiceLoader-backed execution contribution for backend-scoped named commands.
 *
 * <p>Plugins register implementations in {@code
 * META-INF/services/cafe.woden.ircclient.app.commands.spi.BackendNamedCommandExecutor}.
 */
public interface BackendNamedCommandExecutor {

  /** Returns canonical command names without a leading slash. Matching is case-insensitive. */
  Set<String> handledCommandNames();

  /**
   * Executes a portable command request through the app-owned context.
   *
   * @return {@code true} when the command was consumed, including handled validation or connection
   *     failures; {@code false} only when this executor does not handle the request
   */
  boolean handle(BackendNamedCommandExecutionContext context, BackendNamedCommandRequest command);
}
