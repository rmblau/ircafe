package cafe.woden.ircclient.ui.settings.translation;

import cafe.woden.ircclient.ui.localization.UiMessages;
import java.util.Locale;

public enum TranslationServiceChoice {
  DEEPL("preferences.translation.service.deepl", "deepl"),
  LIBRETRANSLATE("preferences.translation.service.libreTranslate", "libretranslate"),
  GOOGLE_WEB("preferences.translation.service.googleWeb", "google-web");

  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();

  private final String labelKey;
  private final String backendId;

  TranslationServiceChoice(String labelKey, String backendId) {
    this.labelKey = labelKey;
    this.backendId = backendId;
  }

  public String backendId() {
    return backendId;
  }

  public String defaultEndpoint() {
    return switch (this) {
      case DEEPL -> "https://api-free.deepl.com/v2/translate";
      case LIBRETRANSLATE -> "https://libretranslate.com/translate";
      case GOOGLE_WEB -> "https://translate.googleapis.com/translate_a/single";
    };
  }

  public boolean apiKeyRequired() {
    return this == DEEPL;
  }

  public static TranslationServiceChoice fromBackendId(String backendId) {
    String normalized = backendId == null ? "" : backendId.trim().toLowerCase(Locale.ROOT);
    for (TranslationServiceChoice choice : values()) {
      if (choice.backendId.equals(normalized)) {
        return choice;
      }
    }
    return DEEPL;
  }

  @Override
  public String toString() {
    return MESSAGES.text(labelKey);
  }
}
