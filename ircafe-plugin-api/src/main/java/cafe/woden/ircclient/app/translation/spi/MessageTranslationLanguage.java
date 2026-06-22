package cafe.woden.ircclient.app.translation.spi;

import java.util.Objects;

/** A target language option exposed for manual message translation. */
public record MessageTranslationLanguage(String code, String label) {
  public MessageTranslationLanguage {
    code = Objects.toString(code, "").trim().toLowerCase(java.util.Locale.ROOT);
    label = Objects.toString(label, "").trim();
  }
}
