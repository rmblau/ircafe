package cafe.woden.ircclient.ui;

import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.jmolecules.architecture.layered.InterfaceLayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Centralizes ServiceLoader-backed external browser plugin contribution points. */
@InterfaceLayer
final class ExternalBrowserPluginProviders {
  private static final Logger log = LoggerFactory.getLogger(ExternalBrowserPluginProviders.class);

  private ExternalBrowserPluginProviders() {}

  static List<ExternalBrowserCommandProvider> commandProviders(
      InstalledPluginsPort installedPlugins) {
    if (installedPlugins == null) {
      return List.of();
    }
    return installedPlugins.loadInstalledServices(ExternalBrowserCommandProvider.class, List.of());
  }

  static Set<String> allowedSchemes(
      InstalledPluginsPort installedPlugins, Set<String> defaultSchemes) {
    LinkedHashSet<String> schemes =
        new LinkedHashSet<>(Objects.requireNonNullElse(defaultSchemes, Set.<String>of()));
    if (installedPlugins == null) {
      return Set.copyOf(schemes);
    }
    for (ExternalBrowserSchemeProvider provider :
        installedPlugins.loadInstalledServices(ExternalBrowserSchemeProvider.class, List.of())) {
      if (provider == null) {
        continue;
      }
      addAllowedSchemes(schemes, provider);
    }
    return Set.copyOf(schemes);
  }

  private static void addAllowedSchemes(
      LinkedHashSet<String> schemes, ExternalBrowserSchemeProvider provider) {
    try {
      for (String scheme :
          Objects.requireNonNullElse(provider.allowedSchemes(), Set.<String>of())) {
        String normalized = ExternalBrowserLauncher.normalizeScheme(scheme);
        if (!normalized.isEmpty()) {
          schemes.add(normalized);
        }
      }
    } catch (RuntimeException e) {
      log.warn("External browser scheme provider failed: {}", provider.getClass().getName(), e);
    }
  }
}
