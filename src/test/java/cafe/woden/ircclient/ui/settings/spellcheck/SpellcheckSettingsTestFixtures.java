package cafe.woden.ircclient.ui.settings.spellcheck;

import java.util.List;

/** Test fixtures for building spellcheck settings without long positional constructors. */
public final class SpellcheckSettingsTestFixtures {

  private SpellcheckSettingsTestFixtures() {}

  public static SpellcheckSettings defaults() {
    return builder().build();
  }

  public static Builder builder() {
    return new Builder(SpellcheckSettings.defaults());
  }

  public static final class Builder {
    private boolean enabled;
    private boolean underlineEnabled;
    private boolean suggestOnTabEnabled;
    private boolean hoverSuggestionsEnabled;
    private String languageTag;
    private List<String> customDictionary;
    private String completionPreset;
    private int customMinPrefixCompletionTokenLength;
    private int customMaxPrefixCompletionExtraChars;
    private int customMaxPrefixLexiconCandidates;
    private int customPrefixCompletionBonusScore;
    private int customSourceOrderWeight;

    private Builder(SpellcheckSettings defaults) {
      this.enabled = defaults.enabled();
      this.underlineEnabled = defaults.underlineEnabled();
      this.suggestOnTabEnabled = defaults.suggestOnTabEnabled();
      this.hoverSuggestionsEnabled = defaults.hoverSuggestionsEnabled();
      this.languageTag = defaults.languageTag();
      this.customDictionary = defaults.customDictionary();
      this.completionPreset = defaults.completionPreset();
      this.customMinPrefixCompletionTokenLength = defaults.customMinPrefixCompletionTokenLength();
      this.customMaxPrefixCompletionExtraChars = defaults.customMaxPrefixCompletionExtraChars();
      this.customMaxPrefixLexiconCandidates = defaults.customMaxPrefixLexiconCandidates();
      this.customPrefixCompletionBonusScore = defaults.customPrefixCompletionBonusScore();
      this.customSourceOrderWeight = defaults.customSourceOrderWeight();
    }

    public Builder enabled(boolean enabled) {
      this.enabled = enabled;
      return this;
    }

    public Builder underlineEnabled(boolean underlineEnabled) {
      this.underlineEnabled = underlineEnabled;
      return this;
    }

    public Builder suggestOnTabEnabled(boolean suggestOnTabEnabled) {
      this.suggestOnTabEnabled = suggestOnTabEnabled;
      return this;
    }

    public Builder hoverSuggestionsEnabled(boolean hoverSuggestionsEnabled) {
      this.hoverSuggestionsEnabled = hoverSuggestionsEnabled;
      return this;
    }

    public Builder languageTag(String languageTag) {
      this.languageTag = languageTag;
      return this;
    }

    public Builder customDictionary(List<String> customDictionary) {
      this.customDictionary = customDictionary;
      return this;
    }

    public Builder completionPreset(String completionPreset) {
      this.completionPreset = completionPreset;
      return this;
    }

    public Builder customMinPrefixCompletionTokenLength(int customMinPrefixCompletionTokenLength) {
      this.customMinPrefixCompletionTokenLength = customMinPrefixCompletionTokenLength;
      return this;
    }

    public Builder customMaxPrefixCompletionExtraChars(int customMaxPrefixCompletionExtraChars) {
      this.customMaxPrefixCompletionExtraChars = customMaxPrefixCompletionExtraChars;
      return this;
    }

    public Builder customMaxPrefixLexiconCandidates(int customMaxPrefixLexiconCandidates) {
      this.customMaxPrefixLexiconCandidates = customMaxPrefixLexiconCandidates;
      return this;
    }

    public Builder customPrefixCompletionBonusScore(int customPrefixCompletionBonusScore) {
      this.customPrefixCompletionBonusScore = customPrefixCompletionBonusScore;
      return this;
    }

    public Builder customSourceOrderWeight(int customSourceOrderWeight) {
      this.customSourceOrderWeight = customSourceOrderWeight;
      return this;
    }

    public SpellcheckSettings build() {
      return new SpellcheckSettings(
          enabled,
          underlineEnabled,
          suggestOnTabEnabled,
          hoverSuggestionsEnabled,
          languageTag,
          customDictionary,
          completionPreset,
          customMinPrefixCompletionTokenLength,
          customMaxPrefixCompletionExtraChars,
          customMaxPrefixLexiconCandidates,
          customPrefixCompletionBonusScore,
          customSourceOrderWeight);
    }
  }
}
