package cafe.woden.ircclient.ui.settings.translation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import cafe.woden.ircclient.app.translation.MessageTranslationSettingsBus;
import cafe.woden.ircclient.config.IrcProperties;
import cafe.woden.ircclient.config.RuntimeConfigStore;
import java.util.List;
import javax.swing.JComboBox;
import org.junit.jupiter.api.Test;

class TranslationControlsSupportTest {

  @Test
  void disabledSettingsClearBackendButKeepEditableDefaults() {
    TranslationControls controls =
        TranslationControlsSupport.buildControls(
            new IrcProperties.Client.Translation(
                false,
                IrcProperties.Client.Translation.Mode.AUTO,
                "",
                "",
                "",
                "auto",
                "",
                null,
                10_000,
                4_000,
                2),
            List.of());

    IrcProperties.Client.Translation settings = TranslationControlsSupport.readSettings(controls);

    assertFalse(settings.enabled());
    assertEquals(IrcProperties.Client.Translation.Mode.AUTO, settings.mode());
    assertEquals("", settings.backendId());
    assertEquals("auto", settings.sourceLanguage());
    assertEquals("", settings.targetLanguage());
    assertTrue(settings.translateUnknownMessages());
    assertTrue(settings.detectAllLanguages());
    assertTrue(settings.detectionLanguages().isEmpty());
  }

  @Test
  void deeplRequiresApiKeyWhenEnabled() {
    TranslationControls controls =
        TranslationControlsSupport.buildControls(
            new IrcProperties.Client.Translation(
                false,
                IrcProperties.Client.Translation.Mode.AUTO,
                "deepl",
                "https://api-free.deepl.com/v2/translate",
                "",
                "auto",
                "es",
                null,
                10_000,
                4_000,
                2),
            List.of());
    controls.enabled().setSelected(true);

    assertThrows(
        TranslationControlsSupport.TranslationSettingsException.class,
        () -> TranslationControlsSupport.readSettings(controls));
  }

  @Test
  void libreTranslateAllowsBlankApiKey() {
    TranslationControls controls =
        TranslationControlsSupport.buildControls(
            new IrcProperties.Client.Translation(
                true,
                IrcProperties.Client.Translation.Mode.MANUAL,
                "libretranslate",
                "https://libretranslate.example.test/translate",
                "",
                "auto",
                "es",
                null,
                10_000,
                4_000,
                2),
            List.of());

    IrcProperties.Client.Translation settings = TranslationControlsSupport.readSettings(controls);

    assertTrue(settings.enabled());
    assertEquals(IrcProperties.Client.Translation.Mode.MANUAL, settings.mode());
    assertEquals("libretranslate", settings.backendId());
    assertEquals("", settings.apiKey());
    assertEquals("es", settings.targetLanguage());
    assertTrue(settings.translateUnknownMessages());
  }

  @Test
  void unknownLanguageTranslationOptionIsPersistedFromCheckbox() {
    TranslationControls controls =
        TranslationControlsSupport.buildControls(
            new IrcProperties.Client.Translation(
                true,
                IrcProperties.Client.Translation.Mode.AUTO,
                "google-web",
                "https://translate.googleapis.com/translate_a/single",
                "",
                "auto",
                "es",
                true,
                null,
                10_000,
                4_000,
                2),
            List.of());
    controls.translateUnknownMessages().setSelected(false);

    IrcProperties.Client.Translation settings = TranslationControlsSupport.readSettings(controls);

    assertFalse(settings.translateUnknownMessages());
  }

  @Test
  void customDetectionLanguagesArePersistedFromEnabledList() {
    TranslationControls controls =
        TranslationControlsSupport.buildControls(
            new IrcProperties.Client.Translation(
                true,
                IrcProperties.Client.Translation.Mode.AUTO,
                "google-web",
                "https://translate.googleapis.com/translate_a/single",
                "",
                "auto",
                "es",
                true,
                false,
                List.of("en", "es"),
                null,
                10_000,
                4_000,
                2),
            List.of());

    IrcProperties.Client.Translation settings = TranslationControlsSupport.readSettings(controls);

    assertFalse(settings.detectAllLanguages());
    assertEquals(List.of("en", "es"), settings.detectionLanguages());
  }

  @Test
  void sourceAndTargetLanguageCombosUseConfiguredDetectionLanguages() {
    TranslationControls controls =
        TranslationControlsSupport.buildControls(
            new IrcProperties.Client.Translation(
                true,
                IrcProperties.Client.Translation.Mode.AUTO,
                "google-web",
                "https://translate.googleapis.com/translate_a/single",
                "",
                "en",
                "es",
                true,
                false,
                List.of("en", "es"),
                null,
                10_000,
                4_000,
                2),
            List.of());

    assertEquals(List.of("auto", "en", "es"), languageCodes(controls.sourceLanguage()));
    assertEquals(List.of("", "en", "es"), languageCodes(controls.targetLanguage()));

    IrcProperties.Client.Translation settings = TranslationControlsSupport.readSettings(controls);
    assertEquals("en", settings.sourceLanguage());
    assertEquals("es", settings.targetLanguage());
  }

  @Test
  void targetLanguageFallsBackWhenNotAvailableInConfiguredDetectionLanguages() {
    TranslationControls controls =
        TranslationControlsSupport.buildControls(
            new IrcProperties.Client.Translation(
                true,
                IrcProperties.Client.Translation.Mode.AUTO,
                "google-web",
                "https://translate.googleapis.com/translate_a/single",
                "",
                "auto",
                "fr",
                true,
                false,
                List.of("en", "es"),
                null,
                10_000,
                4_000,
                2),
            List.of());

    assertEquals(List.of("", "en", "es"), languageCodes(controls.targetLanguage()));
    assertThrows(
        TranslationControlsSupport.TranslationSettingsException.class,
        () -> TranslationControlsSupport.readSettings(controls));
  }

  @Test
  void autoModeRequiresAtLeastTwoCustomDetectionLanguages() {
    TranslationControls controls =
        TranslationControlsSupport.buildControls(
            new IrcProperties.Client.Translation(
                true,
                IrcProperties.Client.Translation.Mode.AUTO,
                "google-web",
                "https://translate.googleapis.com/translate_a/single",
                "",
                "auto",
                "es",
                true,
                false,
                List.of("en"),
                null,
                10_000,
                4_000,
                2),
            List.of());

    assertThrows(
        TranslationControlsSupport.TranslationSettingsException.class,
        () -> TranslationControlsSupport.readSettings(controls));
  }

  @Test
  void googleWebAllowsBlankApiKey() {
    TranslationControls controls =
        TranslationControlsSupport.buildControls(
            new IrcProperties.Client.Translation(
                true,
                IrcProperties.Client.Translation.Mode.AUTO,
                "google-web",
                "https://translate.googleapis.com/translate_a/single",
                "",
                "auto",
                "es",
                null,
                10_000,
                4_000,
                2),
            List.of());

    IrcProperties.Client.Translation settings = TranslationControlsSupport.readSettings(controls);

    assertTrue(settings.enabled());
    assertEquals("google-web", settings.backendId());
    assertEquals("", settings.apiKey());
    assertEquals("es", settings.targetLanguage());
  }

  @Test
  void rememberSettingsUpdatesBusAndRuntimeConfig() {
    IrcProperties.Client.Translation settings =
        new IrcProperties.Client.Translation(
            true,
            IrcProperties.Client.Translation.Mode.AUTO,
            "libretranslate",
            "https://libretranslate.example.test/translate",
            "",
            "auto",
            "es",
            null,
            10_000,
            4_000,
            2);
    MessageTranslationSettingsBus bus =
        new MessageTranslationSettingsBus(new IrcProperties(null, List.of()));
    RuntimeConfigStore runtimeConfig = mock(RuntimeConfigStore.class);

    TranslationControlsSupport.rememberSettings(runtimeConfig, bus, settings);

    assertEquals(settings, bus.get());
    verify(runtimeConfig).rememberClientTranslation(settings);
  }

  private static List<String> languageCodes(JComboBox<TranslationLanguageChoice> combo) {
    java.util.ArrayList<String> codes = new java.util.ArrayList<>();
    for (int i = 0; i < combo.getItemCount(); i++) {
      codes.add(combo.getItemAt(i).code());
    }
    return codes;
  }
}
