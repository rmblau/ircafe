package cafe.woden.ircclient.app.translation;

import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import java.util.List;
import java.util.Objects;
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

  static List<MessageTranslationBackend> translationBackends(
      List<MessageTranslationBackend> builtInBackends, InstalledPluginsPort installedPlugins) {
    List<MessageTranslationBackend> backends = nonNullServices(builtInBackends);
    if (installedPlugins == null) {
      return backends;
    }
    return nonNullServices(
        installedPlugins.loadInstalledServices(MessageTranslationBackend.class, backends));
  }

  static List<MessageTranslationLanguageProvider> languageProviders(
      List<MessageTranslationLanguageProvider> builtInProviders,
      InstalledPluginsPort installedPlugins) {
    List<MessageTranslationLanguageProvider> providers = nonNullServices(builtInProviders);
    if (installedPlugins == null) {
      return providers;
    }
    return nonNullServices(
        installedPlugins.loadInstalledServices(
            MessageTranslationLanguageProvider.class, providers));
  }

  private static <T> List<T> nonNullServices(List<T> services) {
    if (services == null || services.isEmpty()) {
      return List.of();
    }
    return services.stream().filter(Objects::nonNull).toList();
  }
}
