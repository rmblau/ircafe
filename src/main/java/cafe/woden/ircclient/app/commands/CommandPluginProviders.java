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
        dedupeByProviderClass(
            applicationClasspathServices(
                cafe.woden.ircclient.app.commands.spi.SlashCommandParseStrategy.class,
                builtInStrategies));
    if (installedPlugins == null) {
      return strategies;
    }
    return dedupeByProviderClass(
        installedPlugins.loadInstalledServices(
            cafe.woden.ircclient.app.commands.spi.SlashCommandParseStrategy.class, strategies));
  }

  static List<cafe.woden.ircclient.app.commands.spi.SlashCommandPresentationContributor>
      slashCommandPresentationContributors(
          List<? extends cafe.woden.ircclient.app.commands.spi.SlashCommandPresentationContributor>
              builtInContributors,
          InstalledPluginsPort installedPlugins) {
    List<cafe.woden.ircclient.app.commands.spi.SlashCommandPresentationContributor> contributors =
        dedupeByProviderClass(
            applicationClasspathServices(
                cafe.woden.ircclient.app.commands.spi.SlashCommandPresentationContributor.class,
                builtInContributors));
    if (installedPlugins == null) {
      return contributors;
    }
    return dedupeByProviderClass(
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
                nonNullServices(builtInHandlers),
                pluginDirectory,
                applicationClassLoader,
                log);
    return new BackendNamedCommandHandlers(
        dedupeByProviderClass(loadedServices.services()), loadedServices.pluginClassLoaders());
  }

  static BackendNamedCommandHandlers backendNamedCommandHandlers(
      List<? extends cafe.woden.ircclient.app.commands.spi.BackendNamedCommandHandler>
          builtInHandlers,
      InstalledPluginsPort installedPluginsPort) {
    InstalledPluginsPort installedPlugins =
        Objects.requireNonNull(installedPluginsPort, "installedPluginsPort");
    List<cafe.woden.ircclient.app.commands.spi.BackendNamedCommandHandler> handlers =
        nonNullServices(builtInHandlers);
    List<cafe.woden.ircclient.app.commands.spi.BackendNamedCommandHandler> loadedHandlers =
        installedPlugins.loadInstalledServices(
            cafe.woden.ircclient.app.commands.spi.BackendNamedCommandHandler.class, handlers);
    return new BackendNamedCommandHandlers(dedupeByProviderClass(loadedHandlers), List.of());
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
    return new BackendNamedCommandExecutors(
        dedupeByProviderClass(loadedServices.services()), loadedServices.pluginClassLoaders());
  }

  static BackendNamedCommandExecutors backendNamedCommandExecutors(
      List<? extends cafe.woden.ircclient.app.commands.spi.BackendNamedCommandExecutor>
          builtInExecutors,
      InstalledPluginsPort installedPluginsPort) {
    InstalledPluginsPort installedPlugins =
        Objects.requireNonNull(installedPluginsPort, "installedPluginsPort");
    List<cafe.woden.ircclient.app.commands.spi.BackendNamedCommandExecutor> executors =
        nonNullServices(builtInExecutors);
    List<cafe.woden.ircclient.app.commands.spi.BackendNamedCommandExecutor> loadedExecutors =
        installedPlugins.loadInstalledServices(
            cafe.woden.ircclient.app.commands.spi.BackendNamedCommandExecutor.class, executors);
    return new BackendNamedCommandExecutors(dedupeByProviderClass(loadedExecutors), List.of());
  }

  private static <T> List<T> applicationClasspathServices(
      Class<T> serviceType, List<? extends T> builtInServices) {
    return PluginServiceLoaderSupport.loadInstalledServices(
        serviceType,
        nonNullServices(builtInServices),
        PluginServiceLoaderSupport.defaultApplicationClassLoader(CommandPluginProviders.class),
        null);
  }

  private static <T> List<T> nonNullServices(List<? extends T> services) {
    if (services == null || services.isEmpty()) {
      return List.of();
    }
    java.util.ArrayList<T> nonNull = new java.util.ArrayList<>();
    for (T service : services) {
      if (service != null) {
        nonNull.add(service);
      }
    }
    return List.copyOf(nonNull);
  }

  @SafeVarargs
  private static <T> List<T> dedupeByProviderClass(List<? extends T>... serviceGroups) {
    java.util.LinkedHashSet<String> providerClassNames = new java.util.LinkedHashSet<>();
    java.util.ArrayList<T> deduped = new java.util.ArrayList<>();
    for (List<? extends T> services : serviceGroups) {
      if (services == null) {
        continue;
      }
      for (T service : services) {
        if (service == null || !providerClassNames.add(service.getClass().getName())) {
          continue;
        }
        deduped.add(service);
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
