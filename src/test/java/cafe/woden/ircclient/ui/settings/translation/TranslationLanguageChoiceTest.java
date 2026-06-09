package cafe.woden.ircclient.ui.settings.translation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TranslationLanguageChoiceTest {

  @Test
  void builtInChoicesComeFromBundledUiMessages() {
    assertEquals("Auto detect", TranslationLanguageChoice.AUTO.toString());
    assertEquals("Select language", TranslationLanguageChoice.NONE.toString());
  }

  @Test
  void concreteLanguageIncludesLocalizedCodeSuffixFormat() {
    assertEquals("Spanish (es)", new TranslationLanguageChoice("ES", "Spanish").toString());
  }
}
