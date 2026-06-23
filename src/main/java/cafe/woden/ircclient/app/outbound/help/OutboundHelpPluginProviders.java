package cafe.woden.ircclient.app.outbound.help;

import cafe.woden.ircclient.app.outbound.help.spi.OutboundHelpContributor;
import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import java.util.List;
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
    List<OutboundHelpContributor> contributors = dedupeByProviderClass(builtInContributors);
    if (installedPlugins == null) {
      return contributors;
    }
    return dedupeByProviderClass(
        installedPlugins.loadInstalledServices(OutboundHelpContributor.class, contributors));
  }

  private static List<OutboundHelpContributor> dedupeByProviderClass(
      List<? extends OutboundHelpContributor> contributors) {
    if (contributors == null || contributors.isEmpty()) {
      return List.of();
    }
    java.util.LinkedHashSet<String> providerClassNames = new java.util.LinkedHashSet<>();
    java.util.ArrayList<OutboundHelpContributor> deduped = new java.util.ArrayList<>();
    for (OutboundHelpContributor contributor : contributors) {
      if (contributor == null || !providerClassNames.add(contributor.getClass().getName())) {
        continue;
      }
      deduped.add(contributor);
    }
    return List.copyOf(deduped);
  }
}
