package cafe.woden.ircclient.app.api;

import java.util.Objects;

/** Completed translation text for an existing transcript message. */
public record MessageTranslation(
    String targetMessageId,
    String translatedText,
    String sourceLanguage,
    String targetLanguage,
    String provider) {

  public MessageTranslation {
    targetMessageId = Objects.toString(targetMessageId, "").trim();
    translatedText = Objects.toString(translatedText, "");
    sourceLanguage = Objects.toString(sourceLanguage, "").trim();
    targetLanguage = Objects.toString(targetLanguage, "").trim();
    provider = Objects.toString(provider, "").trim();
  }
}
