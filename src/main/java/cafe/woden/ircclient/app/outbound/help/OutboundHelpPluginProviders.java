package cafe.woden.ircclient.app.outbound.help;

import cafe.woden.ircclient.app.outbound.help.spi.OutboundHelpContributor;
import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import java.util.List;
import java.util.Objects;
import org.jmolecules.architecture.layered.ApplicationLayer;
import org.springframework.beans.factory.ObjectProvider;

/** Centralizes ServiceLoader-backed outbound-help plugin provider handling. */
@ApplicationLayer
final class OutboundHelpPluginProviders {
  private OutboundHelpPluginProviders() {}

  static InstalledPluginsPort resolveInstalledPlugins(
      ObjectProvider<InstalledPluginsPort> installedPluginsProvider) {
    return installedPluginsProvider == null ? null : installedPluginsProvider.getIfAvailable();
  }

  static List<OutboundHelpContributor> outboundHelpContributors(
      List<OutboundHelpContributor> builtInContributors, InstalledPluginsPort installedPlugins) {
    List<OutboundHelpContributor> contributors = nonNullContributors(builtInContributors);
    if (installedPlugins == null) {
      return contributors;
    }
    return nonNullContributors(
        installedPlugins.loadInstalledServices(OutboundHelpContributor.class, contributors));
  }

  private static List<OutboundHelpContributor> nonNullContributors(
      List<OutboundHelpContributor> contributors) {
    if (contributors == null || contributors.isEmpty()) {
      return List.of();
    }
    return contributors.stream().filter(Objects::nonNull).toList();
  }
}
