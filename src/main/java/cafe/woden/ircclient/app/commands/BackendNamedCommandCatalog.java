package cafe.woden.ircclient.app.commands;

import cafe.woden.ircclient.app.commands.spi.BackendNamedCommandParseResult;
import cafe.woden.ircclient.app.commands.spi.SlashCommandDescriptor;
import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.config.api.RuntimeConfigPathPort;
import cafe.woden.ircclient.util.PluginServiceLoaderSupport;
import jakarta.annotation.PreDestroy;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.jmolecules.architecture.layered.ApplicationLayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Registry for backend named command contributions from built-ins and ServiceLoader plugins. */
@Component
@ApplicationLayer
public class BackendNamedCommandCatalog {

  private static final Logger log = LoggerFactory.getLogger(BackendNamedCommandCatalog.class);

  private final BackendNamedCommandHandlerRegistry registry;
  private final List<URLClassLoader> pluginClassLoaders;

  @Autowired
  public BackendNamedCommandCatalog(InstalledPluginsPort installedPluginsPort) {
    this(CommandPluginProviders.backendNamedCommandHandlers(List.of(), installedPluginsPort));
  }

  public BackendNamedCommandCatalog(
      RuntimeConfigPathPort runtimeConfigPathPort,
      List<? extends cafe.woden.ircclient.app.commands.spi.BackendNamedCommandHandler>
          builtInHandlers) {
    this(
        CommandPluginProviders.backendNamedCommandHandlers(
            List.copyOf(Objects.requireNonNullElse(builtInHandlers, List.of())),
            PluginServiceLoaderSupport.resolvePluginDirectory(
                runtimeConfigPathPort == null ? null : runtimeConfigPathPort::runtimeConfigPath,
                log),
            PluginServiceLoaderSupport.defaultApplicationClassLoader(
                BackendNamedCommandCatalog.class),
            log));
  }

  public static BackendNamedCommandCatalog empty() {
    return fromHandlers(List.of());
  }

  public static BackendNamedCommandCatalog installed() {
    return installed(
        PluginServiceLoaderSupport.resolvePluginDirectory(null, log),
        PluginServiceLoaderSupport.defaultApplicationClassLoader(BackendNamedCommandCatalog.class));
  }

  public static BackendNamedCommandCatalog fromHandlers(
      List<? extends cafe.woden.ircclient.app.commands.spi.BackendNamedCommandHandler> handlers) {
    return new BackendNamedCommandCatalog(
        List.copyOf(Objects.requireNonNull(handlers, "handlers")), List.of());
  }

  static BackendNamedCommandCatalog installed(
      RuntimeConfigPathPort runtimeConfigPathPort, ClassLoader applicationClassLoader) {
    return installed(
        PluginServiceLoaderSupport.resolvePluginDirectory(
            runtimeConfigPathPort == null ? null : runtimeConfigPathPort::runtimeConfigPath, log),
        applicationClassLoader);
  }

  static BackendNamedCommandCatalog installed(
      Path pluginDirectory, ClassLoader applicationClassLoader) {
    return new BackendNamedCommandCatalog(
        CommandPluginProviders.backendNamedCommandHandlers(
            List.of(), pluginDirectory, applicationClassLoader, log));
  }

  private BackendNamedCommandCatalog(CommandPluginProviders.BackendNamedCommandHandlers state) {
    this(Objects.requireNonNull(state, "state").handlers(), state.pluginClassLoaders());
  }

  private BackendNamedCommandCatalog(
      List<cafe.woden.ircclient.app.commands.spi.BackendNamedCommandHandler> handlers,
      List<URLClassLoader> pluginClassLoaders) {
    this.registry =
        new BackendNamedCommandHandlerRegistry(List.copyOf(Objects.requireNonNull(handlers)));
    this.pluginClassLoaders =
        List.copyOf(Objects.requireNonNull(pluginClassLoaders, "pluginClassLoaders"));
  }

  @PreDestroy
  void shutdown() {
    PluginServiceLoaderSupport.closePluginClassLoaders(
        pluginClassLoaders, log, "[ircafe] failed to close plugin classloader");
  }

  public ParsedInput parse(String line) {
    BackendNamedCommandParseResult parsed = registry.parse(line);
    if (parsed == null) return null;
    return new ParsedInput.BackendNamed(parsed.command(), parsed.args());
  }

  public List<SlashCommandDescriptor> autocompleteCommands() {
    return registry.autocompleteCommands();
  }

  public List<String> generalHelpLines() {
    return registry.generalHelpLines();
  }

  public Map<String, List<String>> topicHelpLines() {
    return registry.topicHelpLines();
  }

  static Path resolvePluginDirectory(RuntimeConfigPathPort runtimeConfigPathPort) {
    return PluginServiceLoaderSupport.resolvePluginDirectory(
        runtimeConfigPathPort == null ? null : runtimeConfigPathPort::runtimeConfigPath, log);
  }
}
