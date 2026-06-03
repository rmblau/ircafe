package cafe.woden.ircclient.ui.settings.translation;

import cafe.woden.ircclient.app.translation.MessageTranslationLanguage;
import java.util.Objects;

/** Combo-box language option for translation preferences. */
public record TranslationLanguageChoice(String code, String label) {
  public static final TranslationLanguageChoice AUTO =
      new TranslationLanguageChoice("auto", "Auto detect");
  public static final TranslationLanguageChoice NONE =
      new TranslationLanguageChoice("", "Select language");

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
    return label + " (" + code + ")";
  }
}
