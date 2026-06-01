package cafe.woden.ircclient.config.api;

import java.util.List;

/** Stores spellcheck and TAB-completion tuning preferences. */
public interface SpellcheckRuntimeConfigPort {
  void rememberSpellcheckEnabled(boolean enabled);

  void rememberSpellcheckUnderlineEnabled(boolean enabled);

  void rememberSpellcheckSuggestOnTabEnabled(boolean enabled);

  void rememberSpellcheckHoverSuggestionsEnabled(boolean enabled);

  void rememberSpellcheckCompletionPreset(String preset);

  void rememberSpellcheckCustomMinPrefixCompletionTokenLength(int value);

  void rememberSpellcheckCustomMaxPrefixCompletionExtraChars(int value);

  void rememberSpellcheckCustomMaxPrefixLexiconCandidates(int value);

  void rememberSpellcheckCustomPrefixCompletionBonusScore(int value);

  void rememberSpellcheckCustomSourceOrderWeight(int value);

  void rememberSpellcheckLanguageTag(String languageTag);

  void rememberSpellcheckCustomDictionary(List<String> words);
}
