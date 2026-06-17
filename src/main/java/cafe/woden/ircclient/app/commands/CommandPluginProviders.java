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
                nonNullServices(builtInExecutors),
                pluginDirectory,
                applicationClassLoader,
                log);
    PluginServiceLoaderSupport.LoadedServices<BackendNamedCommandExecutor> legacyLoadedServices =
        PluginServiceLoaderSupport.loadInstalledServices(
            BackendNamedCommandExecutor.class,
            List.of(),
            pluginDirectory,
            applicationClassLoader,
            log);
    return new BackendNamedCommandExecutors(
        dedupeBackendNamedExecutors(loadedServices.services(), legacyLoadedServices.services()),
        combinedClassLoaders(
            loadedServices.pluginClassLoaders(), legacyLoadedServices.pluginClassLoaders()));
  }

  static BackendNamedCommandExecutors backendNamedCommandExecutors(
      List<? extends cafe.woden.ircclient.app.commands.spi.BackendNamedCommandExecutor>
          builtInExecutors,
      InstalledPluginsPort installedPluginsPort) {
    InstalledPluginsPort installedPlugins =
        Objects.requireNonNull(installedPluginsPort, "installedPluginsPort");
    List<cafe.woden.ircclient.app.commands.spi.BackendNamedCommandExecutor> executors =
        nonNullServices(builtInExecutors);
    ArrayList<cafe.woden.ircclient.app.commands.spi.BackendNamedCommandExecutor> loadedExecutors =
        new ArrayList<>();
    loadedExecutors.addAll(
        installedPlugins.loadInstalledServices(
            cafe.woden.ircclient.app.commands.spi.BackendNamedCommandExecutor.class, executors));
    loadedExecutors.addAll(
        installedPlugins.loadInstalledServices(BackendNamedCommandExecutor.class, List.of()));
    return new BackendNamedCommandExecutors(
        dedupeBackendNamedExecutors(loadedExecutors), List.of());
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

  @SafeVarargs
  private static List<cafe.woden.ircclient.app.commands.spi.BackendNamedCommandExecutor>
      dedupeBackendNamedExecutors(
          List<? extends cafe.woden.ircclient.app.commands.spi.BackendNamedCommandExecutor>...
              executorGroups) {
    java.util.LinkedHashSet<String> providerClassNames = new java.util.LinkedHashSet<>();
    ArrayList<cafe.woden.ircclient.app.commands.spi.BackendNamedCommandExecutor> deduped =
        new ArrayList<>();
    for (List<? extends cafe.woden.ircclient.app.commands.spi.BackendNamedCommandExecutor>
        executors : executorGroups) {
      if (executors == null) {
        continue;
      }
      for (cafe.woden.ircclient.app.commands.spi.BackendNamedCommandExecutor executor : executors) {
        if (executor == null || !providerClassNames.add(executor.getClass().getName())) {
          continue;
        }
        deduped.add(executor);
      }
    }
    return List.copyOf(deduped);
  }

  record BackendNamedCommandHandlers(
      List<cafe.woden.ircclient.app.commands.spi.BackendNamedCommandHandler> handlers,
      List<URLClassLoader> pluginClassLoaders) {}

  record BackendNamedCommandExecutors(
      List<cafe.woden.ircclient.app.commands.spi.BackendNamedCommandExecutor> executors,
      List<URLClassLoader> pluginClassLoaders) {}
}
