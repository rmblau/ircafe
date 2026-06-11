package cafe.woden.ircclient.app.translation;

import cafe.woden.ircclient.config.IrcProperties;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/** Shared target-language choices for manual translation actions. */
public final class MessageTranslationLanguageCatalog {
  private MessageTranslationLanguageCatalog() {}

  private static final List<MessageTranslationLanguage> COMMON_TARGETS =
      List.of(
          new MessageTranslationLanguage("ar", "Arabic"),
          new MessageTranslationLanguage("bg", "Bulgarian"),
          new MessageTranslationLanguage("cs", "Czech"),
          new MessageTranslationLanguage("da", "Danish"),
          new MessageTranslationLanguage("de", "German"),
          new MessageTranslationLanguage("el", "Greek"),
          new MessageTranslationLanguage("en", "English"),
          new MessageTranslationLanguage("es", "Spanish"),
          new MessageTranslationLanguage("et", "Estonian"),
          new MessageTranslationLanguage("fi", "Finnish"),
          new MessageTranslationLanguage("fr", "French"),
          new MessageTranslationLanguage("he", "Hebrew"),
          new MessageTranslationLanguage("hi", "Hindi"),
          new MessageTranslationLanguage("hu", "Hungarian"),
          new MessageTranslationLanguage("id", "Indonesian"),
          new MessageTranslationLanguage("it", "Italian"),
          new MessageTranslationLanguage("ja", "Japanese"),
          new MessageTranslationLanguage("ko", "Korean"),
          new MessageTranslationLanguage("lt", "Lithuanian"),
          new MessageTranslationLanguage("lv", "Latvian"),
          new MessageTranslationLanguage("nl", "Dutch"),
          new MessageTranslationLanguage("pl", "Polish"),
          new MessageTranslationLanguage("pt", "Portuguese"),
          new MessageTranslationLanguage("ro", "Romanian"),
          new MessageTranslationLanguage("ru", "Russian"),
          new MessageTranslationLanguage("sk", "Slovak"),
          new MessageTranslationLanguage("sl", "Slovenian"),
          new MessageTranslationLanguage("sv", "Swedish"),
          new MessageTranslationLanguage("th", "Thai"),
          new MessageTranslationLanguage("tr", "Turkish"),
          new MessageTranslationLanguage("uk", "Ukrainian"),
          new MessageTranslationLanguage("vi", "Vietnamese"),
          new MessageTranslationLanguage("zh", "Chinese"));

  public static List<MessageTranslationLanguage> commonTargets() {
    return COMMON_TARGETS;
  }

  public static List<MessageTranslationLanguage> availableTargets(
      IrcProperties.Client.Translation translation) {
    if (translation == null || translation.detectAllLanguages()) {
      return COMMON_TARGETS;
    }
    Set<String> enabled =
        translation.detectionLanguages().stream()
            .map(code -> Objects.toString(code, "").trim().toLowerCase(java.util.Locale.ROOT))
            .filter(code -> !code.isBlank())
            .collect(Collectors.toSet());
    if (enabled.isEmpty()) {
      return List.of();
    }
    return COMMON_TARGETS.stream().filter(language -> enabled.contains(language.code())).toList();
  }
}
