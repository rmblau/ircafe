package cafe.woden.ircclient.app.translation;

import cafe.woden.ircclient.app.translation.spi.MessageTranslationBackendProvider;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationLanguageProvider;
import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.util.PluginServiceLoaderSupport;
import java.util.List;
import org.jmolecules.architecture.layered.ApplicationLayer;
import org.springframework.beans.factory.ObjectProvider;

/** Centralizes ServiceLoader-backed translation plugin provider handling. */
@ApplicationLayer
final class MessageTranslationPluginProviders {
  private MessageTranslationPluginProviders() {}

  static InstalledPluginsPort resolveInstalledPlugins(
      ObjectProvider<InstalledPluginsPort> installedPluginsProvider) {
    return installedPluginsProvider == null ? null : installedPluginsProvider.getIfAvailable();
  }

  static List<MessageTranslationBackendProvider> translationBackends(
      List<? extends MessageTranslationBackendProvider> builtInBackends,
      InstalledPluginsPort installedPlugins) {
    List<MessageTranslationBackendProvider> seedBackends =
        MessageTranslationProviderCatalog.translationBackends(builtInBackends, List.of());
    if (installedPlugins == null) {
      return seedBackends;
    }
    return MessageTranslationProviderCatalog.translationBackends(
        List.of(),
        installedPlugins.loadInstalledServices(
            MessageTranslationBackendProvider.class, seedBackends));
  }

  static List<MessageTranslationLanguageProvider> builtInLanguageProviders() {
    return MessageTranslationProviderCatalog.languageProviders(
        List.of(),
        PluginServiceLoaderSupport.loadApplicationServices(
            MessageTranslationLanguageProvider.class, MessageTranslationPluginProviders.class));
  }

  static List<MessageTranslationLanguageProvider> languageProviders(
      List<? extends MessageTranslationLanguageProvider> builtInProviders,
      InstalledPluginsPort installedPlugins) {
    List<MessageTranslationLanguageProvider> seedProviders =
        MessageTranslationProviderCatalog.languageProviders(builtInProviders, List.of());
    if (installedPlugins == null) {
      return seedProviders;
    }
    return MessageTranslationProviderCatalog.languageProviders(
        List.of(),
        installedPlugins.loadInstalledServices(
            MessageTranslationLanguageProvider.class, seedProviders));
  }
}
