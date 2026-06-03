package cafe.woden.ircclient.app.translation;

import java.util.Collection;
import java.util.Optional;

/** Detects the likely language of one IRC message body. */
public interface MessageLanguageDetector {

  Optional<String> detectLanguageCode(String text);

  default Optional<String> detectLanguageCode(String text, Collection<String> languageCodes) {
    return detectLanguageCode(text);
  }
}
