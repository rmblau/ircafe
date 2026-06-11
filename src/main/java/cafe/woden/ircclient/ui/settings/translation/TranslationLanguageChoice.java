package cafe.woden.ircclient.ui.settings.translation;

import cafe.woden.ircclient.app.translation.MessageTranslationLanguage;
import cafe.woden.ircclient.ui.localization.UiMessages;
import java.util.Objects;

/** Combo-box language option for translation preferences. */
public record TranslationLanguageChoice(String code, String label) {
  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();

  public static final TranslationLanguageChoice AUTO =
      new TranslationLanguageChoice(
          "auto", MESSAGES.text("preferences.translation.language.autoDetect"));
  public static final TranslationLanguageChoice NONE =
      new TranslationLanguageChoice("", MESSAGES.text("preferences.translation.language.select"));

  public TranslationLanguageChoice {
    code = Objects.toString(code, "").trim().toLowerCase(java.util.Locale.ROOT);
    label = Objects.toString(label, "").trim();
  }

  public static TranslationLanguageChoice from(MessageTranslationLanguage language) {
    return new TranslationLanguageChoice(language.code(), language.label());
  }

  @Override
  public String toString() {
    if (code.isBlank() || "auto".equals(code)) {
      return label;
    }
    return MESSAGES.text("preferences.translation.language.withCode", label, code);
  }
}
