package cafe.woden.ircclient.app.translation;

import java.util.Objects;
import org.jmolecules.ddd.annotation.ValueObject;

/** Backend response for one translated message. */
@ValueObject
public record MessageTranslationResult(
    String translatedText, String sourceLanguage, String targetLanguage, String provider) {

  public MessageTranslationResult {
    translatedText = Objects.toString(translatedText, "");
    sourceLanguage = Objects.toString(sourceLanguage, "").trim();
    targetLanguage = Objects.toString(targetLanguage, "").trim();
    provider = Objects.toString(provider, "").trim();
  }
}
