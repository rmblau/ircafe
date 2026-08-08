package cafe.woden.ircclient.app.translation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import cafe.woden.ircclient.app.translation.spi.MessageTranslationLanguage;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationLanguageProvider;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class MessageTranslationLanguageCatalogFeatureTest {

  @Test
  void commonTargetsNormalizeAndDedupeProviderLanguages() {
    MessageTranslationLanguageProvider builtIn =
        () ->
            Arrays.asList(
                new MessageTranslationLanguage(" EN_us ", "English"),
                new MessageTranslationLanguage("es", "Spanish"),
                null,
                new MessageTranslationLanguage("", "Missing"));
    MessageTranslationLanguageProvider plugin =
        () ->
            List.of(
                new MessageTranslationLanguage("en-US", "Plugin English"),
                new MessageTranslationLanguage("tlh", ""));

    List<MessageTranslationLanguage> languages =
        MessageTranslationLanguageCatalog.commonTargets(Arrays.asList(null, builtIn, plugin));

    assertEquals(
        List.of(
            new MessageTranslationLanguage("en-us", "English"),
            new MessageTranslationLanguage("es", "Spanish"),
            new MessageTranslationLanguage("tlh", "tlh")),
        languages);
  }

  @Test
  void availableTargetsFiltersByDetectionLanguageSettingsSnapshot() {
    MessageTranslationSettingsSnapshot settings =
        new MessageTranslationSettingsSnapshot(
            true,
            MessageTranslationSettingsSnapshot.Mode.AUTO,
            "test",
            "",
            "",
            "auto",
            "es",
            true,
            false,
            List.of(" ES ", "TLH"),
            10_000,
            4_000,
            2);
    List<MessageTranslationLanguage> languages =
        List.of(
            new MessageTranslationLanguage("en", "English"),
            new MessageTranslationLanguage("es", "Spanish"),
            new MessageTranslationLanguage("tlh", "Klingon"));

    assertEquals(
        List.of(
            new MessageTranslationLanguage("es", "Spanish"),
            new MessageTranslationLanguage("tlh", "Klingon")),
        MessageTranslationLanguageCatalog.availableTargets(settings, languages));
  }

  @Test
  void availableTargetsReturnsEmptyWhenDetectionLanguagesAreDisabledAndBlank() {
    List<MessageTranslationLanguage> languages =
        List.of(new MessageTranslationLanguage("en", "English"));

    assertEquals(
        List.of(), MessageTranslationLanguageCatalog.availableTargets(false, List.of(" "), languages));
  }
}
