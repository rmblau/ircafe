package cafe.woden.ircclient.app.translation;

import cafe.woden.ircclient.app.translation.builtins.CommonMessageTranslationLanguageProvider;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationLanguageProvider;
import cafe.woden.ircclient.config.IrcProperties;
import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/** Shared target-language choices for manual translation actions. */
public final class MessageTranslationLanguageCatalog {
  private MessageTranslationLanguageCatalog() {}

  private static final List<MessageTranslationLanguageProvider> BUILT_IN_PROVIDERS =
      List.of(new CommonMessageTranslationLanguageProvider());

  private static final List<MessageTranslationLanguage> COMMON_TARGETS =
      mergeLanguages(BUILT_IN_PROVIDERS);

  public static List<MessageTranslationLanguage> commonTargets() {
    return COMMON_TARGETS;
  }

  public static List<MessageTranslationLanguage> commonTargets(
      InstalledPluginsPort installedPlugins) {
    if (installedPlugins == null) {
      return COMMON_TARGETS;
    }
    return mergeLanguages(
        MessageTranslationPluginProviders.languageProviders(BUILT_IN_PROVIDERS, installedPlugins));
  }

  public static List<MessageTranslationLanguage> availableTargets(
      IrcProperties.Client.Translation translation) {
    return availableTargets(translation, COMMON_TARGETS);
  }

  public static List<MessageTranslationLanguage> availableTargets(
      IrcProperties.Client.Translation translation, InstalledPluginsPort installedPlugins) {
    return availableTargets(translation, commonTargets(installedPlugins));
  }

  private static List<MessageTranslationLanguage> availableTargets(
      IrcProperties.Client.Translation translation, List<MessageTranslationLanguage> languages) {
    if (translation == null || translation.detectAllLanguages()) {
      return languages;
    }
    Set<String> enabled =
        translation.detectionLanguages().stream()
            .map(MessageTranslationLanguageCatalog::normalizeCode)
            .filter(code -> !code.isBlank())
            .collect(Collectors.toSet());
    if (enabled.isEmpty()) {
      return List.of();
    }
    return languages.stream().filter(language -> enabled.contains(language.code())).toList();
  }

  private static List<MessageTranslationLanguage> mergeLanguages(
      List<? extends MessageTranslationLanguageProvider> providers) {
    Map<String, MessageTranslationLanguage> byCode = new LinkedHashMap<>();
    List<? extends MessageTranslationLanguageProvider> safeProviders =
        providers == null ? List.of() : providers;
    for (MessageTranslationLanguageProvider provider : safeProviders) {
      if (provider == null) {
        continue;
      }
      List<MessageTranslationLanguage> languages = provider.languages();
      if (languages == null) {
        continue;
      }
      for (MessageTranslationLanguage language : languages) {
        if (language == null) {
          continue;
        }
        String code = normalizeCode(language.code());
        if (code.isBlank()) {
          continue;
        }
        String label = Objects.toString(language.label(), "").trim();
        if (label.isBlank()) {
          label = code;
        }
        byCode.putIfAbsent(code, new MessageTranslationLanguage(code, label));
      }
    }
    return List.copyOf(byCode.values());
  }

  private static String normalizeCode(String code) {
    return Objects.toString(code, "").trim().toLowerCase(Locale.ROOT).replace('_', '-');
  }
}
