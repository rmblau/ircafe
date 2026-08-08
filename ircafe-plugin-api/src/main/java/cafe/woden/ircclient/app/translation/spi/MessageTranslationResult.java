package cafe.woden.ircclient.app.translation.spi;

import java.util.Objects;

/**
 * Backend response for one translated message.
 *
 * <p>A blank translated text is treated as an unusable result. Source language, target language,
 * and provider may be blank; IRCafe falls back to request/backend metadata when it renders a usable
 * result.
 */
public record MessageTranslationResult(
    String translatedText, String sourceLanguage, String targetLanguage, String provider) {

  public MessageTranslationResult {
    translatedText = Objects.toString(translatedText, "");
    sourceLanguage = Objects.toString(sourceLanguage, "").trim();
    targetLanguage = Objects.toString(targetLanguage, "").trim();
    provider = Objects.toString(provider, "").trim();
  }
}
