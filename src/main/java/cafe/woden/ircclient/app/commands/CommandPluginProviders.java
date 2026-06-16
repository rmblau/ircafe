package cafe.woden.ircclient.app.commands;

import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import java.util.List;
import java.util.Objects;
import org.jmolecules.architecture.layered.ApplicationLayer;
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

  private static <T> List<T> nonNullServices(List<T> services) {
    if (services == null || services.isEmpty()) {
      return List.of();
    }
    return services.stream().filter(Objects::nonNull).toList();
  }
}
