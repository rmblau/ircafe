package cafe.woden.ircclient.app.translation;

import cafe.woden.ircclient.app.translation.spi.MessageTranslationBackendProvider;
import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import java.util.List;
import org.jmolecules.architecture.layered.InfrastructureLayer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@InfrastructureLayer
@Configuration(proxyBeanMethods = false)
class MessageTranslationBackendRegistryConfiguration {

  @Bean
  MessageTranslationBackendRegistry messageTranslationBackendRegistry(
      List<MessageTranslationBackendProvider> backends,
      ObjectProvider<InstalledPluginsPort> installedPluginsProvider) {
    return new MessageTranslationBackendRegistry(
        MessageTranslationPluginProviders.translationBackends(
            backends,
            MessageTranslationPluginProviders.resolveInstalledPlugins(installedPluginsProvider)));
  }
}
