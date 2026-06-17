package cafe.woden.ircclient.app.translation;

import cafe.woden.ircclient.app.translation.spi.MessageTranslationBackendProvider;
import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import java.util.ArrayList;
import java.util.LinkedHashSet;
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
    List<MessageTranslationBackendProvider> backends = nonNullServices(builtInBackends);
    if (installedPlugins == null) {
      return backends;
    }
    ArrayList<MessageTranslationBackendProvider> providers = new ArrayList<>();
    providers.addAll(
        installedPlugins.loadInstalledServices(MessageTranslationBackendProvider.class, backends));
    providers.addAll(
        installedPlugins.loadInstalledServices(MessageTranslationBackend.class, List.of()));
    return dedupeProviders(nonNullServices(providers));
  }

  static List<cafe.woden.ircclient.app.translation.spi.MessageTranslationLanguageProvider>
      languageProviders(
          List<
                  ? extends
                      cafe.woden.ircclient.app.translation.spi.MessageTranslationLanguageProvider>
              builtInProviders,
          InstalledPluginsPort installedPlugins) {
    List<cafe.woden.ircclient.app.translation.spi.MessageTranslationLanguageProvider> providers =
        nonNullServices(builtInProviders);
    if (installedPlugins == null) {
      return providers;
    }
    ArrayList<cafe.woden.ircclient.app.translation.spi.MessageTranslationLanguageProvider>
        loadedProviders = new ArrayList<>();
    loadedProviders.addAll(
        installedPlugins.loadInstalledServices(
            cafe.woden.ircclient.app.translation.spi.MessageTranslationLanguageProvider.class,
            providers));
    loadedProviders.addAll(
        installedPlugins.loadInstalledServices(
            MessageTranslationLanguageProvider.class, List.of()));
    return nonNullServices(loadedProviders);
  }

  private static List<MessageTranslationBackendProvider> dedupeProviders(
      List<MessageTranslationBackendProvider> providers) {
    LinkedHashSet<String> providerKeys = new LinkedHashSet<>();
    ArrayList<MessageTranslationBackendProvider> deduped = new ArrayList<>();
    for (MessageTranslationBackendProvider provider : providers) {
      if (provider == null) {
        continue;
      }
      String providerKey =
          provider.getClass().getName()
              + '\u0000'
              + MessageTranslationBackendRegistry.normalizeBackendId(provider.backendId());
      if (!providerKeys.add(providerKey)) {
        continue;
      }
      deduped.add(provider);
    }
    return List.copyOf(deduped);
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
}
