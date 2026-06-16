package cafe.woden.ircclient.ui.settings.spellcheck;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import cafe.woden.ircclient.config.api.SpellcheckRuntimeConfigPort;
import java.util.List;
import org.junit.jupiter.api.Test;

class SpellcheckSettingsTest {

  @Test
  void defaultsUseAndroidLikePresetProfile() {
    SpellcheckSettings settings = SpellcheckSettings.defaults();
    SpellcheckSettings.CompletionProfile profile = settings.completionProfile();

    assertFalse(settings.hoverSuggestionsEnabled());
    assertEquals(SpellcheckSettings.COMPLETION_PRESET_ANDROID_LIKE, settings.completionPreset());
    assertEquals(
        SpellcheckSettings.DEFAULT_CUSTOM_MIN_PREFIX_COMPLETION_TOKEN_LENGTH,
        profile.minPrefixCompletionTokenLength());
    assertEquals(
        SpellcheckSettings.DEFAULT_CUSTOM_MAX_PREFIX_COMPLETION_EXTRA_CHARS,
        profile.maxPrefixCompletionExtraChars());
    assertEquals(
        SpellcheckSettings.DEFAULT_CUSTOM_MAX_PREFIX_LEXICON_CANDIDATES,
        profile.maxPrefixLexiconCandidates());
    assertEquals(
        SpellcheckSettings.DEFAULT_CUSTOM_PREFIX_COMPLETION_BONUS_SCORE,
        profile.prefixCompletionBonusScore());
    assertEquals(
        SpellcheckSettings.DEFAULT_CUSTOM_SOURCE_ORDER_WEIGHT, profile.sourceOrderWeight());
  }

  @Test
  void conservativePresetUsesPresetValuesInsteadOfCustomOverrides() {
    SpellcheckSettings settings =
        SpellcheckSettingsTestFixtures.builder()
            .hoverSuggestionsEnabled(true)
            .completionPreset(SpellcheckSettings.COMPLETION_PRESET_CONSERVATIVE)
            .customMinPrefixCompletionTokenLength(2)
            .customMaxPrefixCompletionExtraChars(24)
            .customMaxPrefixLexiconCandidates(256)
            .customPrefixCompletionBonusScore(400)
            .customSourceOrderWeight(0)
            .build();
    SpellcheckSettings.CompletionProfile profile = settings.completionProfile();

    assertEquals(3, profile.minPrefixCompletionTokenLength());
    assertEquals(9, profile.maxPrefixCompletionExtraChars());
    assertEquals(64, profile.maxPrefixLexiconCandidates());
    assertEquals(140, profile.prefixCompletionBonusScore());
    assertEquals(8, profile.sourceOrderWeight());
  }

  @Test
  void standardPresetUsesBalancedValues() {
    SpellcheckSettings settings =
        SpellcheckSettingsTestFixtures.builder()
            .hoverSuggestionsEnabled(true)
            .completionPreset(SpellcheckSettings.COMPLETION_PRESET_STANDARD)
            .customMinPrefixCompletionTokenLength(2)
            .customMaxPrefixCompletionExtraChars(24)
            .customMaxPrefixLexiconCandidates(256)
            .customPrefixCompletionBonusScore(400)
            .customSourceOrderWeight(0)
            .build();
    SpellcheckSettings.CompletionProfile profile = settings.completionProfile();

    assertEquals(2, profile.minPrefixCompletionTokenLength());
    assertEquals(12, profile.maxPrefixCompletionExtraChars());
    assertEquals(80, profile.maxPrefixLexiconCandidates());
    assertEquals(180, profile.prefixCompletionBonusScore());
    assertEquals(7, profile.sourceOrderWeight());
  }

  @Test
  void customPresetNormalizesOutOfRangeKnobs() {
    SpellcheckSettings settings =
        SpellcheckSettingsTestFixtures.builder()
            .hoverSuggestionsEnabled(true)
            .completionPreset(SpellcheckSettings.COMPLETION_PRESET_CUSTOM)
            .customMinPrefixCompletionTokenLength(1)
            .customMaxPrefixCompletionExtraChars(100)
            .customMaxPrefixLexiconCandidates(999)
            .customPrefixCompletionBonusScore(-25)
            .customSourceOrderWeight(99)
            .build();
    SpellcheckSettings.CompletionProfile profile = settings.completionProfile();

    assertEquals(2, profile.minPrefixCompletionTokenLength());
    assertEquals(24, profile.maxPrefixCompletionExtraChars());
    assertEquals(256, profile.maxPrefixLexiconCandidates());
    assertEquals(0, profile.prefixCompletionBonusScore());
    assertEquals(20, profile.sourceOrderWeight());
  }

  @Test
  void normalizeCompletionPresetFallsBackToAndroidLike() {
    assertEquals(
        SpellcheckSettings.COMPLETION_PRESET_ANDROID_LIKE,
        SpellcheckSettings.normalizeCompletionPreset("unknown"));
  }

  @Test
  void normalizeCompletionPresetAcceptsStandard() {
    assertEquals(
        SpellcheckSettings.COMPLETION_PRESET_STANDARD,
        SpellcheckSettings.normalizeCompletionPreset("standard"));
  }

  @Test
  void readSettingsBuildsSpellcheckSettingsFromControls() {
    SpellcheckControls controls =
        SpellcheckControlsSupport.buildControls(SpellcheckSettings.defaults());
    controls.enabled.setSelected(false);
    controls.underlineEnabled.setSelected(false);
    controls.suggestOnTabEnabled.setSelected(true);
    controls.hoverSuggestionsEnabled.setSelected(true);
    controls.languageTag.setSelectedItem(new SpellcheckLanguageOption("en-GB", "English (UK)"));
    controls.customDictionary.setText(" Foo\nbar baz\nfoo ");
    controls.completionPreset.setSelectedItem(
        new SpellcheckPresetOption(SpellcheckSettings.COMPLETION_PRESET_CUSTOM, "Custom"));
    controls.customMinPrefixCompletionTokenLength.setValue(3);
    controls.customMaxPrefixCompletionExtraChars.setValue(12);
    controls.customMaxPrefixLexiconCandidates.setValue(64);
    controls.customPrefixCompletionBonusScore.setValue(200);
    controls.customSourceOrderWeight.setValue(7);

    SpellcheckSettings settings = SpellcheckControlsSupport.readSettings(controls);

    assertFalse(settings.enabled());
    assertFalse(settings.underlineEnabled());
    assertTrue(settings.suggestOnTabEnabled());
    assertTrue(settings.hoverSuggestionsEnabled());
    assertEquals("en-GB", settings.languageTag());
    assertEquals(List.of("Foo", "bar", "baz"), settings.customDictionary());
    assertEquals(SpellcheckSettings.COMPLETION_PRESET_CUSTOM, settings.completionPreset());
    assertEquals(3, settings.customMinPrefixCompletionTokenLength());
    assertEquals(12, settings.customMaxPrefixCompletionExtraChars());
    assertEquals(64, settings.customMaxPrefixLexiconCandidates());
    assertEquals(200, settings.customPrefixCompletionBonusScore());
    assertEquals(7, settings.customSourceOrderWeight());
  }

  @Test
  void rememberSettingsPersistsSpellcheckSettings() {
    SpellcheckRuntimeConfigPort runtimeConfig = mock(SpellcheckRuntimeConfigPort.class);
    SpellcheckSettings settings =
        SpellcheckSettingsTestFixtures.builder()
            .enabled(false)
            .underlineEnabled(false)
            .suggestOnTabEnabled(true)
            .hoverSuggestionsEnabled(true)
            .languageTag("en-GB")
            .customDictionary(List.of("IRCafe"))
            .completionPreset(SpellcheckSettings.COMPLETION_PRESET_CUSTOM)
            .customMinPrefixCompletionTokenLength(3)
            .customMaxPrefixCompletionExtraChars(12)
            .customMaxPrefixLexiconCandidates(64)
            .customPrefixCompletionBonusScore(200)
            .customSourceOrderWeight(7)
            .build();

    SpellcheckControlsSupport.rememberSettings(runtimeConfig, settings);

    verify(runtimeConfig).rememberSpellcheckEnabled(false);
    verify(runtimeConfig).rememberSpellcheckUnderlineEnabled(false);
    verify(runtimeConfig).rememberSpellcheckSuggestOnTabEnabled(true);
    verify(runtimeConfig).rememberSpellcheckHoverSuggestionsEnabled(true);
    verify(runtimeConfig).rememberSpellcheckLanguageTag("en-GB");
    verify(runtimeConfig).rememberSpellcheckCustomDictionary(List.of("IRCafe"));
    verify(runtimeConfig)
        .rememberSpellcheckCompletionPreset(SpellcheckSettings.COMPLETION_PRESET_CUSTOM);
    verify(runtimeConfig).rememberSpellcheckCustomMinPrefixCompletionTokenLength(3);
    verify(runtimeConfig).rememberSpellcheckCustomMaxPrefixCompletionExtraChars(12);
    verify(runtimeConfig).rememberSpellcheckCustomMaxPrefixLexiconCandidates(64);
    verify(runtimeConfig).rememberSpellcheckCustomPrefixCompletionBonusScore(200);
    verify(runtimeConfig).rememberSpellcheckCustomSourceOrderWeight(7);
  }
}
