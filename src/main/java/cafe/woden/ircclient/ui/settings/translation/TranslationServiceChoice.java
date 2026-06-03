package cafe.woden.ircclient.ui.settings.translation;

import java.util.Locale;

public enum TranslationServiceChoice {
  DEEPL("DeepL", "deepl"),
  LIBRETRANSLATE("LibreTranslate", "libretranslate"),
  GOOGLE_WEB("Google Web (unofficial)", "google-web");

  private final String label;
  private final String backendId;

  TranslationServiceChoice(String label, String backendId) {
    this.label = label;
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
    return label;
  }
}
