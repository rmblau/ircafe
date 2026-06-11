package cafe.woden.ircclient.ui.settings.translation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TranslationServiceChoiceTest {

  @Test
  void labelsComeFromBundledUiMessages() {
    assertEquals("DeepL", TranslationServiceChoice.DEEPL.toString());
    assertEquals("LibreTranslate", TranslationServiceChoice.LIBRETRANSLATE.toString());
    assertEquals("Google Web (unofficial)", TranslationServiceChoice.GOOGLE_WEB.toString());
  }

  @Test
  void backendMetadataIsPreserved() {
    assertEquals("deepl", TranslationServiceChoice.DEEPL.backendId());
    assertEquals("google-web", TranslationServiceChoice.GOOGLE_WEB.backendId());
    assertTrue(TranslationServiceChoice.DEEPL.apiKeyRequired());
  }
}
