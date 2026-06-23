package cafe.woden.ircclient.app.commands;

import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.config.api.RuntimeConfigPathPort;
import cafe.woden.ircclient.util.PluginServiceLoaderSupport;
import jakarta.annotation.PreDestroy;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.jmolecules.architecture.layered.ApplicationLayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Registry for backend named command execution contributions from built-ins and plugins. */
@Component
@ApplicationLayer
public final class BackendNamedCommandExecutorCatalog {

  private static final Logger log =
      LoggerFactory.getLogger(BackendNamedCommandExecutorCatalog.class);

  private final Map<String, cafe.woden.ircclient.app.commands.spi.BackendNamedCommandExecutor>
      executionHandlersByCommandName;
  private final List<URLClassLoader> pluginClassLoaders;

  @Autowired
  public BackendNamedCommandExecutorCatalog(
      InstalledPluginsPort installedPluginsPort,
      List<cafe.woden.ircclient.app.commands.spi.BackendNamedCommandExecutor> builtInExecutors) {
    this(
        CommandPluginProviders.backendNamedCommandExecutors(
            List.copyOf(Objects.requireNonNullElse(builtInExecutors, List.of())),
            installedPluginsPort));
  }

  public BackendNamedCommandExecutorCatalog(
      RuntimeConfigPathPort runtimeConfigPathPort,
      List<? extends cafe.woden.ircclient.app.commands.spi.BackendNamedCommandExecutor>
          builtInExecutors) {
    this(
        CommandPluginProviders.backendNamedCommandExecutors(
            List.copyOf(Objects.requireNonNullElse(builtInExecutors, List.of())),
            PluginServiceLoaderSupport.resolvePluginDirectory(
                runtimeConfigPathPort == null ? null : runtimeConfigPathPort::runtimeConfigPath,
                log),
            PluginServiceLoaderSupport.defaultApplicationClassLoader(
                BackendNamedCommandExecutorCatalog.class),
            log));
  }

  public static BackendNamedCommandExecutorCatalog empty() {
    return fromExecutors(List.of());
  }

  public static BackendNamedCommandExecutorCatalog installed() {
    return installed(
        PluginServiceLoaderSupport.resolvePluginDirectory(null, log),
        PluginServiceLoaderSupport.defaultApplicationClassLoader(
            BackendNamedCommandExecutorCatalog.class));
  }

  public static BackendNamedCommandExecutorCatalog fromExecutors(
      List<? extends cafe.woden.ircclient.app.commands.spi.BackendNamedCommandExecutor> executors) {
    return new BackendNamedCommandExecutorCatalog(
        List.copyOf(Objects.requireNonNull(executors, "executors")), List.of());
  }

  static BackendNamedCommandExecutorCatalog installed(
      Path pluginDirectory, ClassLoader applicationClassLoader) {
    return new BackendNamedCommandExecutorCatalog(
        CommandPluginProviders.backendNamedCommandExecutors(
            List.of(), pluginDirectory, applicationClassLoader, log));
  }

  private BackendNamedCommandExecutorCatalog(
      CommandPluginProviders.BackendNamedCommandExecutors state) {
    this(Objects.requireNonNull(state, "state").executors(), state.pluginClassLoaders());
  }

  private BackendNamedCommandExecutorCatalog(
      List<cafe.woden.ircclient.app.commands.spi.BackendNamedCommandExecutor> executors,
      List<URLClassLoader> pluginClassLoaders) {
    this.executionHandlersByCommandName = indexExecutionHandlersByCommandName(executors);
    this.pluginClassLoaders =
        List.copyOf(Objects.requireNonNull(pluginClassLoaders, "pluginClassLoaders"));
  }

  @PreDestroy
  void shutdown() {
    PluginServiceLoaderSupport.closePluginClassLoaders(
        pluginClassLoaders, log, "[ircafe] failed to close backend execution plugin classloader");
  }

  public boolean handle(
      cafe.woden.ircclient.app.commands.spi.BackendNamedCommandExecutionContext context,
      ParsedInput.BackendNamed command) {
    if (context == null || command == null) return false;
    cafe.woden.ircclient.app.commands.spi.BackendNamedCommandExecutor executor =
        executionHandlersByCommandName.get(
            BackendNamedCommandRegistrationSupport.normalizeCommandName(command.command()));
    if (executor == null) return false;
    return executor.handle(
        context,
        new cafe.woden.ircclient.app.commands.spi.BackendNamedCommandRequest(
            command.command(), command.args()));
  }

  private static Map<String, cafe.woden.ircclient.app.commands.spi.BackendNamedCommandExecutor>
      indexExecutionHandlersByCommandName(
          List<cafe.woden.ircclient.app.commands.spi.BackendNamedCommandExecutor> executors) {
    LinkedHashMap<String, cafe.woden.ircclient.app.commands.spi.BackendNamedCommandExecutor> index =
        new LinkedHashMap<>();
    for (cafe.woden.ircclient.app.commands.spi.BackendNamedCommandExecutor executor :
        Objects.requireNonNullElse(
            executors,
            List.<cafe.woden.ircclient.app.commands.spi.BackendNamedCommandExecutor>of())) {
      if (executor == null) continue;
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
        cafe.woden.ircclient.app.commands.spi.BackendNamedCommandExecutor previous =
            index.putIfAbsent(normalized, executor);
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
