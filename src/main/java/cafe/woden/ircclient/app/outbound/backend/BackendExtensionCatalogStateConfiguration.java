package cafe.woden.ircclient.app.outbound.backend;

import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.irc.ircv3.Ircv3MessageMutationRuntimeCatalog;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.layered.InfrastructureLayer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@InfrastructureLayer
@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
class BackendExtensionCatalogStateConfiguration {
  @NonNull private final InstalledPluginsPort installedPluginsPort;
  @NonNull private final Ircv3MessageMutationRuntimeCatalog mutationRuntimeCatalog;

  @Bean
  BackendExtensionCatalogState backendExtensionCatalogState() {
    return BackendExtensionCatalogState.fromInstalledServices(
        installedPluginsPort, mutationRuntimeCatalog);
  }
}
