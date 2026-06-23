package cafe.woden.ircclient.ui;

import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.ui.spi.ExternalBrowserCommandProvider;
import cafe.woden.ircclient.ui.spi.ExternalBrowserSchemeProvider;
import cafe.woden.ircclient.util.PluginServiceLoaderSupport;
import java.util.ArrayList;
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
  private static final List<ExternalBrowserSchemeProvider> BUILT_IN_SCHEME_PROVIDERS =
      PluginServiceLoaderSupport.loadInstalledServices(
          ExternalBrowserSchemeProvider.class,
          List.of(),
          PluginServiceLoaderSupport.defaultApplicationClassLoader(
              ExternalBrowserPluginProviders.class),
          (ClassLoader) null);

  private ExternalBrowserPluginProviders() {}

  static List<ExternalBrowserCommandProvider> commandProviders(
      InstalledPluginsPort installedPlugins) {
    if (installedPlugins == null) {
      return List.of();
    }
    return dedupeByProviderClass(
        installedPlugins.loadInstalledServices(
            cafe.woden.ircclient.ui.spi.ExternalBrowserCommandProvider.class, List.of()));
  }

  static Set<String> allowedSchemes(InstalledPluginsPort installedPlugins) {
    LinkedHashSet<String> schemes = new LinkedHashSet<>();
    for (ExternalBrowserSchemeProvider provider : schemeProviders(installedPlugins)) {
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

  static List<ExternalBrowserSchemeProvider> schemeProviders(
      InstalledPluginsPort installedPlugins) {
    if (installedPlugins == null) {
      return BUILT_IN_SCHEME_PROVIDERS;
    }
    return dedupeByProviderClass(
        installedPlugins.loadInstalledServices(
            ExternalBrowserSchemeProvider.class, BUILT_IN_SCHEME_PROVIDERS));
  }

  private static <T> List<T> dedupeByProviderClass(List<? extends T> services) {
    LinkedHashSet<String> providerClassNames = new LinkedHashSet<>();
    ArrayList<T> deduped = new ArrayList<>();
    for (T service : Objects.requireNonNullElse(services, List.<T>of())) {
      if (service == null || !providerClassNames.add(service.getClass().getName())) {
        continue;
      }
      deduped.add(service);
    }
    return List.copyOf(deduped);
  }
}
