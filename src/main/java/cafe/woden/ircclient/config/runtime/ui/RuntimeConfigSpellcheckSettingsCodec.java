package cafe.woden.ircclient.config.runtime.ui;

import cafe.woden.ircclient.config.properties.UiProperties;

/** Pure normalization helpers for persisted spellcheck settings. */
final class RuntimeConfigSpellcheckSettingsCodec {

  private RuntimeConfigSpellcheckSettingsCodec() {}

  static String normalizeCompletionPreset(String preset) {
    return UiProperties.normalizeSpellcheckCompletionPreset(preset);
  }

  static int normalizeCustomMinPrefixCompletionTokenLength(int value) {
    return clamp(value, 2, 6);
  }

  static int normalizeCustomMaxPrefixCompletionExtraChars(int value) {
    return clamp(value, 4, 24);
  }

  static int normalizeCustomMaxPrefixLexiconCandidates(int value) {
    return clamp(value, 16, 256);
  }

  static int normalizeCustomPrefixCompletionBonusScore(int value) {
    return clamp(value, 0, 400);
  }

  static int normalizeCustomSourceOrderWeight(int value) {
    return clamp(value, 0, 20);
  }

  static String normalizeLanguageTag(String languageTag) {
    return UiProperties.normalizeSpellcheckLanguageTag(languageTag);
  }

  private static int clamp(int value, int min, int max) {
    return Math.max(min, Math.min(max, value));
  }
}
