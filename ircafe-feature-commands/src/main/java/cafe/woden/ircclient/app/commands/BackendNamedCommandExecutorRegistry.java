package cafe.woden.ircclient.app.commands;

import cafe.woden.ircclient.app.commands.spi.BackendNamedCommandExecutionContext;
import cafe.woden.ircclient.app.commands.spi.BackendNamedCommandExecutor;
import cafe.woden.ircclient.app.commands.spi.BackendNamedCommandRequest;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Feature-owned registry for backend-scoped named command execution providers. */
public final class BackendNamedCommandExecutorRegistry {

  private final Map<String, BackendNamedCommandExecutor> executorsByCommandName;

  public BackendNamedCommandExecutorRegistry(
      List<? extends BackendNamedCommandExecutor> executors) {
    this.executorsByCommandName = indexExecutorsByCommandName(copyNonNullExecutors(executors));
  }

  public boolean handle(
      BackendNamedCommandExecutionContext context, BackendNamedCommandRequest command) {
    if (context == null || command == null) return false;
    BackendNamedCommandExecutor executor =
        executorsByCommandName.get(
            BackendNamedCommandRegistrationSupport.normalizeCommandName(command.command()));
    if (executor == null) return false;
    return executor.handle(context, command);
  }

  private static List<BackendNamedCommandExecutor> copyNonNullExecutors(
      List<? extends BackendNamedCommandExecutor> executors) {
    if (executors == null || executors.isEmpty()) {
      return List.of();
    }
    ArrayList<BackendNamedCommandExecutor> nonNull = new ArrayList<>();
    for (BackendNamedCommandExecutor executor : executors) {
      if (executor != null) {
        nonNull.add(executor);
      }
    }
    return List.copyOf(nonNull);
  }

  private static Map<String, BackendNamedCommandExecutor> indexExecutorsByCommandName(
      List<BackendNamedCommandExecutor> executors) {
    LinkedHashMap<String, BackendNamedCommandExecutor> index = new LinkedHashMap<>();
    for (BackendNamedCommandExecutor executor : executors) {
      Set<String> commandNames =
          Objects.requireNonNullElse(executor.handledCommandNames(), Set.<String>of());
      for (String commandName : commandNames) {
        String normalized =
            BackendNamedCommandRegistrationSupport.normalizeCommandName(commandName);
        if (normalized.isEmpty()) continue;
        if (BackendNamedCommandRegistrationSupport.isReservedCommandName(normalized)) {
          throw new IllegalStateException(
              "Backend named execution command '"
                  + normalized
                  + "' collides with a reserved built-in command");
        }
        BackendNamedCommandExecutor previous = index.putIfAbsent(normalized, executor);
        if (previous != null && previous != executor) {
          throw new IllegalStateException(
              "Duplicate backend named execution handler registered for command '"
                  + normalized
                  + "'");
        }
      }
    }
    return Map.copyOf(index);
  }
}
