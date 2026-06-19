package cafe.woden.ircclient.app.commands.spi;

import cafe.woden.ircclient.app.commands.BackendNamedCommandExecutionContext;
import cafe.woden.ircclient.app.commands.ParsedInput;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import java.util.Set;

/**
 * ServiceLoader-backed execution contribution for backend-scoped named commands.
 *
 * <p>Plugins register implementations in {@code
 * META-INF/services/cafe.woden.ircclient.app.commands.spi.BackendNamedCommandExecutor}.
 */
public interface BackendNamedCommandExecutor {

  Set<String> handledCommandNames();

  boolean handle(
      BackendNamedCommandExecutionContext context,
      CompositeDisposable disposables,
      ParsedInput.BackendNamed command);
}
