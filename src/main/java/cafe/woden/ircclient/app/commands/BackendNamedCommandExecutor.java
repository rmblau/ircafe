package cafe.woden.ircclient.app.commands;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import java.util.Set;
import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.architecture.layered.ApplicationLayer;

/**
 * ServiceLoader-backed execution contribution for backend-scoped named commands.
 *
 * <p>Plugins register implementations in {@code
 * META-INF/services/cafe.woden.ircclient.app.commands.BackendNamedCommandExecutor}.
 */
@SecondaryPort
@ApplicationLayer
public interface BackendNamedCommandExecutor {

  Set<String> handledCommandNames();

  boolean handle(
      BackendNamedCommandExecutionContext context,
      CompositeDisposable disposables,
      ParsedInput.BackendNamed command);
}
