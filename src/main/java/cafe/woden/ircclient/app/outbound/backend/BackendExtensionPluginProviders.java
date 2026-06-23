package cafe.woden.ircclient.app.outbound.backend;

import cafe.woden.ircclient.app.outbound.backend.spi.BackendExtension;
import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.util.PluginServiceLoaderSupport;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import org.jmolecules.architecture.layered.ApplicationLayer;

/** Centralizes ServiceLoader-backed backend extension provider handling. */
@ApplicationLayer
final class BackendExtensionPluginProviders {
  private BackendExtensionPluginProviders() {}

  static List<BackendExtension> backendExtensions(
      List<? extends BackendExtension> builtInExtensions, InstalledPluginsPort installedPlugins) {
    List<BackendExtension> extensions =
        applicationClasspathServices(BackendExtension.class, builtInExtensions);
    if (installedPlugins == null) {
      return extensions;
    }
    return dedupeByProviderClass(
        installedPlugins.loadInstalledServices(BackendExtension.class, extensions));
  }

  private static <T> List<T> applicationClasspathServices(
      Class<T> serviceType, List<? extends T> builtInServices) {
    return PluginServiceLoaderSupport.loadInstalledServices(
        serviceType,
        nonNullServices(builtInServices),
        PluginServiceLoaderSupport.defaultApplicationClassLoader(
            BackendExtensionPluginProviders.class),
        null);
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

  private static List<BackendExtension> dedupeByProviderClass(
      List<? extends BackendExtension> extensions) {
    LinkedHashSet<String> providerClassNames = new LinkedHashSet<>();
    ArrayList<BackendExtension> deduped = new ArrayList<>();
    for (BackendExtension extension : nonNullServices(extensions)) {
      if (providerClassNames.add(extension.getClass().getName())) {
        deduped.add(extension);
      }
    }
    return List.copyOf(deduped);
  }
}
