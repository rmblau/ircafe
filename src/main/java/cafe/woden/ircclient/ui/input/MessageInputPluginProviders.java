package cafe.woden.ircclient.ui.input;

import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.ui.input.spi.MatrixUploadMsgTypeProvider;
import cafe.woden.ircclient.ui.input.spi.MessageInputSpellcheckDictionaryProvider;
import cafe.woden.ircclient.ui.input.spi.MessageInputWordSuggestionProvider;
import cafe.woden.ircclient.util.PluginServiceLoaderSupport;
import java.util.List;
import org.jmolecules.architecture.layered.InterfaceLayer;

/** Centralizes ServiceLoader-backed message input plugin contribution points. */
@InterfaceLayer
final class MessageInputPluginProviders {
  private MessageInputPluginProviders() {}

  static List<MatrixUploadMsgTypeProvider> matrixUploadMsgTypeProviders(
      InstalledPluginsPort installedPlugins) {
    List<MatrixUploadMsgTypeProvider> providers = builtInMatrixUploadMsgTypeProviders();
    if (installedPlugins == null) {
      return providers;
    }
    return dedupeByProviderClass(
        installedPlugins.loadInstalledServices(MatrixUploadMsgTypeProvider.class, providers));
  }

  static List<MessageInputSpellcheckDictionaryProvider> spellcheckDictionaryProviders(
      InstalledPluginsPort installedPlugins) {
    if (installedPlugins == null) {
      return List.of();
    }
    return installedPlugins.loadInstalledServices(
        MessageInputSpellcheckDictionaryProvider.class, List.of());
  }

  static MessageInputWordSuggestionProvider wordSuggestionProvider(
      MessageInputWordSuggestionProvider builtInProvider, InstalledPluginsPort installedPlugins) {
    return CompositeMessageInputWordSuggestionProvider.from(builtInProvider, installedPlugins);
  }

  static List<MatrixUploadMsgTypeProvider> builtInMatrixUploadMsgTypeProviders() {
    return PluginServiceLoaderSupport.loadInstalledServices(
        MatrixUploadMsgTypeProvider.class,
        List.of(),
        PluginServiceLoaderSupport.defaultApplicationClassLoader(MessageInputPluginProviders.class),
        null);
  }

  private static <T> List<T> dedupeByProviderClass(List<? extends T> services) {
    java.util.LinkedHashSet<String> providerClassNames = new java.util.LinkedHashSet<>();
    java.util.ArrayList<T> deduped = new java.util.ArrayList<>();
    for (T service : java.util.Objects.requireNonNullElse(services, List.<T>of())) {
      if (service == null || !providerClassNames.add(service.getClass().getName())) {
        continue;
      }
      deduped.add(service);
    }
    return List.copyOf(deduped);
  }
}
