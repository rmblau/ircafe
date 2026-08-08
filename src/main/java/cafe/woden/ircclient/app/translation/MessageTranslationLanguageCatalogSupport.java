package cafe.woden.ircclient.app.translation;

import cafe.woden.ircclient.app.translation.spi.MessageTranslationLanguage;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationLanguageProvider;
import cafe.woden.ircclient.config.IrcProperties;
import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import java.util.List;

/** Root app facade that adds plugin loading and config adaptation to the feature catalog. */
public final class MessageTranslationLanguageCatalogSupport {
  private MessageTranslationLanguageCatalogSupport() {}

  private static final List<MessageTranslationLanguageProvider> BUILT_IN_PROVIDERS =
      MessageTranslationPluginProviders.builtInLanguageProviders();

  private static final List<MessageTranslationLanguage> COMMON_TARGETS =
      MessageTranslationLanguageCatalog.commonTargets(BUILT_IN_PROVIDERS);

  public static List<MessageTranslationLanguage> commonTargets() {
    return COMMON_TARGETS;
  }

  public static List<MessageTranslationLanguage> commonTargets(
      InstalledPluginsPort installedPlugins) {
    if (installedPlugins == null) {
      return COMMON_TARGETS;
    }
    return MessageTranslationLanguageCatalog.commonTargets(
        MessageTranslationPluginProviders.languageProviders(BUILT_IN_PROVIDERS, installedPlugins));
  }

  public static List<MessageTranslationLanguage> availableTargets(
      IrcProperties.Client.Translation translation) {
    return MessageTranslationLanguageCatalog.availableTargets(
        MessageTranslationSettingsBus.snapshot(translation), COMMON_TARGETS);
  }

  public static List<MessageTranslationLanguage> availableTargets(
      IrcProperties.Client.Translation translation, InstalledPluginsPort installedPlugins) {
    return MessageTranslationLanguageCatalog.availableTargets(
        MessageTranslationSettingsBus.snapshot(translation), commonTargets(installedPlugins));
  }

  public static List<MessageTranslationLanguage> availableTargets(
      MessageTranslationSettingsSnapshot translation, InstalledPluginsPort installedPlugins) {
    return MessageTranslationLanguageCatalog.availableTargets(
        translation, commonTargets(installedPlugins));
  }
}
