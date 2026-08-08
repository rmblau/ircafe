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

  static List<cafe.woden.ircclient.app.commands.spi.SlashCommandParseStrategy>
      slashCommandParseStrategies(
          List<? extends cafe.woden.ircclient.app.commands.spi.SlashCommandParseStrategy>
              builtInStrategies,
          InstalledPluginsPort installedPlugins) {
    List<cafe.woden.ircclient.app.commands.spi.SlashCommandParseStrategy> strategies =
        CommandProviderCatalog.slashCommandParseStrategies(
            PluginServiceLoaderSupport.loadApplicationServices(
                cafe.woden.ircclient.app.commands.spi.SlashCommandParseStrategy.class,
                builtInStrategies,
                CommandPluginProviders.class));
    if (installedPlugins == null) {
      return strategies;
    }
    return CommandProviderCatalog.slashCommandParseStrategies(
        installedPlugins.loadInstalledServices(
            cafe.woden.ircclient.app.commands.spi.SlashCommandParseStrategy.class, strategies));
  }

  static List<cafe.woden.ircclient.app.commands.spi.SlashCommandPresentationContributor>
      slashCommandPresentationContributors(
          List<? extends cafe.woden.ircclient.app.commands.spi.SlashCommandPresentationContributor>
              builtInContributors,
          InstalledPluginsPort installedPlugins) {
    List<cafe.woden.ircclient.app.commands.spi.SlashCommandPresentationContributor> contributors =
        CommandProviderCatalog.slashCommandPresentationContributors(
            PluginServiceLoaderSupport.loadApplicationServices(
                cafe.woden.ircclient.app.commands.spi.SlashCommandPresentationContributor.class,
                builtInContributors,
                CommandPluginProviders.class));
    if (installedPlugins == null) {
      return contributors;
    }
    return CommandProviderCatalog.slashCommandPresentationContributors(
        installedPlugins.loadInstalledServices(
            cafe.woden.ircclient.app.commands.spi.SlashCommandPresentationContributor.class,
            contributors));
  }

  static BackendNamedCommandHandlers backendNamedCommandHandlers(
      List<? extends cafe.woden.ircclient.app.commands.spi.BackendNamedCommandHandler>
          builtInHandlers,
      Path pluginDirectory,
      ClassLoader applicationClassLoader,
      Logger log) {
    PluginServiceLoaderSupport.LoadedServices<
            cafe.woden.ircclient.app.commands.spi.BackendNamedCommandHandler>
        loadedServices =
            PluginServiceLoaderSupport.loadInstalledServices(
                cafe.woden.ircclient.app.commands.spi.BackendNamedCommandHandler.class,
                CommandProviderCatalog.backendNamedCommandHandlers(builtInHandlers),
                pluginDirectory,
                applicationClassLoader,
                log);
    return new BackendNamedCommandHandlers(
        CommandProviderCatalog.backendNamedCommandHandlers(loadedServices.services()),
        loadedServices.pluginClassLoaders());
  }

  static BackendNamedCommandHandlers backendNamedCommandHandlers(
      List<? extends cafe.woden.ircclient.app.commands.spi.BackendNamedCommandHandler>
          builtInHandlers,
      InstalledPluginsPort installedPluginsPort) {
    InstalledPluginsPort installedPlugins =
        Objects.requireNonNull(installedPluginsPort, "installedPluginsPort");
    List<cafe.woden.ircclient.app.commands.spi.BackendNamedCommandHandler> handlers =
        CommandProviderCatalog.backendNamedCommandHandlers(
            PluginServiceLoaderSupport.loadApplicationServices(
                cafe.woden.ircclient.app.commands.spi.BackendNamedCommandHandler.class,
                builtInHandlers,
                CommandPluginProviders.class));
    List<cafe.woden.ircclient.app.commands.spi.BackendNamedCommandHandler> loadedHandlers =
        installedPlugins.loadInstalledServices(
            cafe.woden.ircclient.app.commands.spi.BackendNamedCommandHandler.class, handlers);
    return new BackendNamedCommandHandlers(
        CommandProviderCatalog.backendNamedCommandHandlers(loadedHandlers), List.of());
  }

  static BackendNamedCommandExecutors backendNamedCommandExecutors(
      List<? extends cafe.woden.ircclient.app.commands.spi.BackendNamedCommandExecutor>
          builtInExecutors,
      Path pluginDirectory,
      ClassLoader applicationClassLoader,
      Logger log) {
    PluginServiceLoaderSupport.LoadedServices<
            cafe.woden.ircclient.app.commands.spi.BackendNamedCommandExecutor>
        loadedServices =
            PluginServiceLoaderSupport.loadInstalledServices(
                cafe.woden.ircclient.app.commands.spi.BackendNamedCommandExecutor.class,
                CommandProviderCatalog.backendNamedCommandExecutors(builtInExecutors),
                pluginDirectory,
                applicationClassLoader,
                log);
    return new BackendNamedCommandExecutors(
        CommandProviderCatalog.backendNamedCommandExecutors(loadedServices.services()),
        loadedServices.pluginClassLoaders());
  }

  static BackendNamedCommandExecutors backendNamedCommandExecutors(
      List<? extends cafe.woden.ircclient.app.commands.spi.BackendNamedCommandExecutor>
          builtInExecutors,
      InstalledPluginsPort installedPluginsPort) {
    InstalledPluginsPort installedPlugins =
        Objects.requireNonNull(installedPluginsPort, "installedPluginsPort");
    List<cafe.woden.ircclient.app.commands.spi.BackendNamedCommandExecutor> executors =
        CommandProviderCatalog.backendNamedCommandExecutors(
            PluginServiceLoaderSupport.loadApplicationServices(
                cafe.woden.ircclient.app.commands.spi.BackendNamedCommandExecutor.class,
                builtInExecutors,
                CommandPluginProviders.class));
    List<cafe.woden.ircclient.app.commands.spi.BackendNamedCommandExecutor> loadedExecutors =
        installedPlugins.loadInstalledServices(
            cafe.woden.ircclient.app.commands.spi.BackendNamedCommandExecutor.class, executors);
    return new BackendNamedCommandExecutors(
        CommandProviderCatalog.backendNamedCommandExecutors(loadedExecutors), List.of());
  }

  record BackendNamedCommandHandlers(
      List<cafe.woden.ircclient.app.commands.spi.BackendNamedCommandHandler> handlers,
      List<URLClassLoader> pluginClassLoaders) {}

  record BackendNamedCommandExecutors(
      List<cafe.woden.ircclient.app.commands.spi.BackendNamedCommandExecutor> executors,
      List<URLClassLoader> pluginClassLoaders) {}
}
