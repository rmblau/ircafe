package cafe.woden.ircclient.app.outbound.backend;

import cafe.woden.ircclient.app.outbound.backend.spi.BackendExtension;
import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.util.PluginServiceLoaderSupport;
import java.util.List;
import org.jmolecules.architecture.layered.ApplicationLayer;

/** Centralizes ServiceLoader-backed backend extension provider handling. */
@ApplicationLayer
final class BackendExtensionPluginProviders {
  private BackendExtensionPluginProviders() {}

  static List<BackendExtension> applicationClasspathBackendExtensions() {
    return PluginServiceLoaderSupport.loadApplicationServices(
        BackendExtension.class, BackendExtensionPluginProviders.class);
  }

  static List<BackendExtension> backendExtensions(InstalledPluginsPort installedPlugins) {
    List<BackendExtension> extensions = applicationClasspathBackendExtensions();
    if (installedPlugins == null) {
      return extensions;
    }
    return PluginServiceLoaderSupport.dedupeByProviderClass(
        installedPlugins.loadInstalledServices(BackendExtension.class, extensions));
  }
}
