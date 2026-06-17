package cafe.woden.ircclient.app.commands;

import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.util.PluginServiceLoaderSupport;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
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
        nonNullServices(builtInStrategies);
    if (installedPlugins == null) {
      return strategies;
    }
    ArrayList<cafe.woden.ircclient.app.commands.spi.SlashCommandParseStrategy> loadedStrategies =
        new ArrayList<>();
    loadedStrategies.addAll(
        installedPlugins.loadInstalledServices(
            cafe.woden.ircclient.app.commands.spi.SlashCommandParseStrategy.class, strategies));
    loadedStrategies.addAll(
        installedPlugins.loadInstalledServices(SlashCommandParseStrategy.class, List.of()));
    return nonNullServices(loadedStrategies);
  }

  static List<cafe.woden.ircclient.app.commands.spi.SlashCommandPresentationContributor>
      slashCommandPresentationContributors(
          List<? extends cafe.woden.ircclient.app.commands.spi.SlashCommandPresentationContributor>
              builtInContributors,
          InstalledPluginsPort installedPlugins) {
    List<cafe.woden.ircclient.app.commands.spi.SlashCommandPresentationContributor> contributors =
        nonNullServices(builtInContributors);
    if (installedPlugins == null) {
      return contributors;
    }
    ArrayList<cafe.woden.ircclient.app.commands.spi.SlashCommandPresentationContributor>
        loadedContributors = new ArrayList<>();
    loadedContributors.addAll(
        installedPlugins.loadInstalledServices(
            cafe.woden.ircclient.app.commands.spi.SlashCommandPresentationContributor.class,
            contributors));
    loadedContributors.addAll(
        installedPlugins.loadInstalledServices(
            SlashCommandPresentationContributor.class, List.of()));
    return nonNullServices(loadedContributors);
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
                nonNullServices(builtInHandlers),
                pluginDirectory,
                applicationClassLoader,
                log);
    PluginServiceLoaderSupport.LoadedServices<BackendNamedCommandHandler> legacyLoadedServices =
        PluginServiceLoaderSupport.loadInstalledServices(
            BackendNamedCommandHandler.class,
            List.of(),
            pluginDirectory,
            applicationClassLoader,
            log);
    return new BackendNamedCommandHandlers(
        dedupeBackendNamedHandlers(loadedServices.services(), legacyLoadedServices.services()),
        combinedClassLoaders(
            loadedServices.pluginClassLoaders(), legacyLoadedServices.pluginClassLoaders()));
  }

  static BackendNamedCommandHandlers backendNamedCommandHandlers(
      List<? extends cafe.woden.ircclient.app.commands.spi.BackendNamedCommandHandler>
          builtInHandlers,
      InstalledPluginsPort installedPluginsPort) {
    InstalledPluginsPort installedPlugins =
        Objects.requireNonNull(installedPluginsPort, "installedPluginsPort");
    List<cafe.woden.ircclient.app.commands.spi.BackendNamedCommandHandler> handlers =
        nonNullServices(builtInHandlers);
    ArrayList<cafe.woden.ircclient.app.commands.spi.BackendNamedCommandHandler> loadedHandlers =
        new ArrayList<>();
    loadedHandlers.addAll(
        installedPlugins.loadInstalledServices(
            cafe.woden.ircclient.app.commands.spi.BackendNamedCommandHandler.class, handlers));
    loadedHandlers.addAll(
        installedPlugins.loadInstalledServices(BackendNamedCommandHandler.class, List.of()));
    return new BackendNamedCommandHandlers(dedupeBackendNamedHandlers(loadedHandlers), List.of());
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

  private static <T> List<T> nonNullServices(List<? extends T> services) {
    if (services == null || services.isEmpty()) {
      return List.of();
    }
    ArrayList<T> nonNull = new ArrayList<>();
    for (T service : services) {
      if (service != null) {
        nonNull.add(service);
      }
    }
    return List.copyOf(nonNull);
  }

  @SafeVarargs
  private static List<cafe.woden.ircclient.app.commands.spi.BackendNamedCommandHandler>
      dedupeBackendNamedHandlers(
          List<? extends cafe.woden.ircclient.app.commands.spi.BackendNamedCommandHandler>...
              handlerGroups) {
    java.util.LinkedHashSet<String> providerClassNames = new java.util.LinkedHashSet<>();
    ArrayList<cafe.woden.ircclient.app.commands.spi.BackendNamedCommandHandler> deduped =
        new ArrayList<>();
    for (List<? extends cafe.woden.ircclient.app.commands.spi.BackendNamedCommandHandler> handlers :
        handlerGroups) {
      if (handlers == null) {
        continue;
      }
      for (cafe.woden.ircclient.app.commands.spi.BackendNamedCommandHandler handler : handlers) {
        if (handler == null || !providerClassNames.add(handler.getClass().getName())) {
          continue;
        }
        deduped.add(handler);
      }
    }
    return List.copyOf(deduped);
  }

  private static List<URLClassLoader> combinedClassLoaders(
      List<URLClassLoader> first, List<URLClassLoader> second) {
    ArrayList<URLClassLoader> classLoaders = new ArrayList<>();
    classLoaders.addAll(Objects.requireNonNullElse(first, List.of()));
    classLoaders.addAll(Objects.requireNonNullElse(second, List.of()));
    return List.copyOf(classLoaders);
  }

  record BackendNamedCommandHandlers(
      List<cafe.woden.ircclient.app.commands.spi.BackendNamedCommandHandler> handlers,
      List<URLClassLoader> pluginClassLoaders) {}

  record BackendNamedCommandExecutors(
      List<BackendNamedCommandExecutor> executors, List<URLClassLoader> pluginClassLoaders) {}
}
