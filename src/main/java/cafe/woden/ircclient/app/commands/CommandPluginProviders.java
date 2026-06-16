package cafe.woden.ircclient.app.commands;

import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.util.PluginServiceLoaderSupport;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import org.jmolecules.architecture.layered.ApplicationLayer;
import org.slf4j.Logger;
import org.springframework.beans.factory.ObjectProvider;

/** Centralizes ServiceLoader-backed slash-command plugin provider handling. */
@ApplicationLayer
final class CommandPluginProviders {
  private CommandPluginProviders() {}

  static InstalledPluginsPort resolveInstalledPlugins(
      ObjectProvider<InstalledPluginsPort> installedPluginsProvider) {
    return installedPluginsProvider == null ? null : installedPluginsProvider.getIfAvailable();
  }

  static List<SlashCommandParseStrategy> slashCommandParseStrategies(
      List<SlashCommandParseStrategy> builtInStrategies, InstalledPluginsPort installedPlugins) {
    List<SlashCommandParseStrategy> strategies = nonNullServices(builtInStrategies);
    if (installedPlugins == null) {
      return strategies;
    }
    return installedPlugins.loadInstalledServices(SlashCommandParseStrategy.class, strategies);
  }

  static List<SlashCommandPresentationContributor> slashCommandPresentationContributors(
      List<SlashCommandPresentationContributor> builtInContributors,
      InstalledPluginsPort installedPlugins) {
    List<SlashCommandPresentationContributor> contributors = nonNullServices(builtInContributors);
    if (installedPlugins == null) {
      return contributors;
    }
    return installedPlugins.loadInstalledServices(
        SlashCommandPresentationContributor.class, contributors);
  }

  static BackendNamedCommandHandlers backendNamedCommandHandlers(
      List<BackendNamedCommandHandler> builtInHandlers,
      Path pluginDirectory,
      ClassLoader applicationClassLoader,
      Logger log) {
    PluginServiceLoaderSupport.LoadedServices<BackendNamedCommandHandler> loadedServices =
        PluginServiceLoaderSupport.loadInstalledServices(
            BackendNamedCommandHandler.class,
            List.copyOf(Objects.requireNonNullElse(builtInHandlers, List.of())),
            pluginDirectory,
            applicationClassLoader,
            log);
    return new BackendNamedCommandHandlers(
        loadedServices.services(), loadedServices.pluginClassLoaders());
  }

  static BackendNamedCommandHandlers backendNamedCommandHandlers(
      List<BackendNamedCommandHandler> builtInHandlers, InstalledPluginsPort installedPluginsPort) {
    InstalledPluginsPort installedPlugins =
        Objects.requireNonNull(installedPluginsPort, "installedPluginsPort");
    List<BackendNamedCommandHandler> handlers =
        List.copyOf(Objects.requireNonNullElse(builtInHandlers, List.of()));
    return new BackendNamedCommandHandlers(
        installedPlugins.loadInstalledServices(BackendNamedCommandHandler.class, handlers),
        List.of());
  }

  static BackendNamedCommandExecutors backendNamedCommandExecutors(
      List<BackendNamedCommandExecutor> builtInExecutors,
      Path pluginDirectory,
      ClassLoader applicationClassLoader,
      Logger log) {
    PluginServiceLoaderSupport.LoadedServices<BackendNamedCommandExecutor> loadedServices =
        PluginServiceLoaderSupport.loadInstalledServices(
            BackendNamedCommandExecutor.class,
            List.copyOf(Objects.requireNonNullElse(builtInExecutors, List.of())),
            pluginDirectory,
            applicationClassLoader,
            log);
    return new BackendNamedCommandExecutors(
        loadedServices.services(), loadedServices.pluginClassLoaders());
  }

  static BackendNamedCommandExecutors backendNamedCommandExecutors(
      List<BackendNamedCommandExecutor> builtInExecutors,
      InstalledPluginsPort installedPluginsPort) {
    InstalledPluginsPort installedPlugins =
        Objects.requireNonNull(installedPluginsPort, "installedPluginsPort");
    List<BackendNamedCommandExecutor> executors =
        List.copyOf(Objects.requireNonNullElse(builtInExecutors, List.of()));
    return new BackendNamedCommandExecutors(
        installedPlugins.loadInstalledServices(BackendNamedCommandExecutor.class, executors),
        List.of());
  }

  private static <T> List<T> nonNullServices(List<T> services) {
    if (services == null || services.isEmpty()) {
      return List.of();
    }
    return services.stream().filter(Objects::nonNull).toList();
  }

  record BackendNamedCommandHandlers(
      List<BackendNamedCommandHandler> handlers, List<URLClassLoader> pluginClassLoaders) {}

  record BackendNamedCommandExecutors(
      List<BackendNamedCommandExecutor> executors, List<URLClassLoader> pluginClassLoaders) {}
}
