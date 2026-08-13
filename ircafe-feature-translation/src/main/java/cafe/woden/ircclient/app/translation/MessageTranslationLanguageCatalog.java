package cafe.woden.ircclient.app.translation;

import cafe.woden.ircclient.app.translation.spi.MessageTranslationLanguage;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationLanguageProvider;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/** Shared target-language choices for translation actions. */
public final class MessageTranslationLanguageCatalog {
  private MessageTranslationLanguageCatalog() {}

  public static List<MessageTranslationLanguage> commonTargets(
      List<? extends MessageTranslationLanguageProvider> providers) {
    return mergeLanguages(providers);
  }

  public static List<MessageTranslationLanguage> availableTargets(
      MessageTranslationSettingsSnapshot translation, List<MessageTranslationLanguage> languages) {
    return availableTargets(
        translation == null || translation.detectAllLanguages(),
        translation == null ? List.of() : translation.detectionLanguages(),
        languages);
  }

  public static List<MessageTranslationLanguage> availableTargets(
      boolean detectAllLanguages,
      List<String> detectionLanguages,
      List<MessageTranslationLanguage> languages) {
    List<MessageTranslationLanguage> safeLanguages = languages == null ? List.of() : languages;
    if (detectAllLanguages) {
      return List.copyOf(safeLanguages);
    }
    Set<String> enabled =
        (detectionLanguages == null ? List.<String>of() : detectionLanguages)
            .stream()
                .map(MessageTranslationLanguageCatalog::normalizeCode)
                .filter(code -> !code.isBlank())
                .collect(Collectors.toSet());
    if (enabled.isEmpty()) {
      return List.of();
    }
    return safeLanguages.stream().filter(language -> enabled.contains(language.code())).toList();
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
