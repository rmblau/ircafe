package cafe.woden.ircclient.app.translation.spi;

import java.util.Objects;

/** Backend response for one translated message. */
public record MessageTranslationResult(
    String translatedText, String sourceLanguage, String targetLanguage, String provider) {

  public MessageTranslationResult {
    translatedText = Objects.toString(translatedText, "");
    sourceLanguage = Objects.toString(sourceLanguage, "").trim();
    targetLanguage = Objects.toString(targetLanguage, "").trim();
    provider = Objects.toString(provider, "").trim();
  }
}
