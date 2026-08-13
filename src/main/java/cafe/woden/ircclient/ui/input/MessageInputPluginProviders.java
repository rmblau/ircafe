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
    return PluginServiceLoaderSupport.dedupeByProviderClass(
        installedPlugins.loadInstalledServices(MatrixUploadMsgTypeProvider.class, providers));
  }

  static List<MessageInputSpellcheckDictionaryProvider> spellcheckDictionaryProviders(
      InstalledPluginsPort installedPlugins) {
    if (installedPlugins == null) {
      return List.of();
    }
    return PluginServiceLoaderSupport.dedupeByProviderClass(
        installedPlugins.loadInstalledServices(
            MessageInputSpellcheckDictionaryProvider.class, List.of()));
  }

  static MessageInputWordSuggestionProvider wordSuggestionProvider(
      MessageInputWordSuggestionProvider builtInProvider, InstalledPluginsPort installedPlugins) {
    return CompositeMessageInputWordSuggestionProvider.from(builtInProvider, installedPlugins);
  }

  static List<MatrixUploadMsgTypeProvider> builtInMatrixUploadMsgTypeProviders() {
    return PluginServiceLoaderSupport.dedupeByProviderClass(
        PluginServiceLoaderSupport.loadApplicationServices(
            MatrixUploadMsgTypeProvider.class, MessageInputPluginProviders.class));
  }
}
