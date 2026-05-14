package cafe.woden.ircclient.ui.settings.spellcheck;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;

record SpellcheckLanguageOption(String id, String label) {}

record SpellcheckPresetOption(String id, String label) {}

public final class SpellcheckControls {
  final JCheckBox enabled;
  final JCheckBox underlineEnabled;
  final JCheckBox suggestOnTabEnabled;
  final JCheckBox hoverSuggestionsEnabled;
  final JComboBox<SpellcheckLanguageOption> languageTag;
  final JTextArea customDictionary;
  final JComboBox<SpellcheckPresetOption> completionPreset;
  final JSpinner customMinPrefixCompletionTokenLength;
  final JSpinner customMaxPrefixCompletionExtraChars;
  final JSpinner customMaxPrefixLexiconCandidates;
  final JSpinner customPrefixCompletionBonusScore;
  final JSpinner customSourceOrderWeight;
  private final JPanel panel;

  SpellcheckControls(
      JCheckBox enabled,
      JCheckBox underlineEnabled,
      JCheckBox suggestOnTabEnabled,
      JCheckBox hoverSuggestionsEnabled,
      JComboBox<SpellcheckLanguageOption> languageTag,
      JTextArea customDictionary,
      JComboBox<SpellcheckPresetOption> completionPreset,
      JSpinner customMinPrefixCompletionTokenLength,
      JSpinner customMaxPrefixCompletionExtraChars,
      JSpinner customMaxPrefixLexiconCandidates,
      JSpinner customPrefixCompletionBonusScore,
      JSpinner customSourceOrderWeight,
      JPanel panel) {
    this.enabled = enabled;
    this.underlineEnabled = underlineEnabled;
    this.suggestOnTabEnabled = suggestOnTabEnabled;
    this.hoverSuggestionsEnabled = hoverSuggestionsEnabled;
    this.languageTag = languageTag;
    this.customDictionary = customDictionary;
    this.completionPreset = completionPreset;
    this.customMinPrefixCompletionTokenLength = customMinPrefixCompletionTokenLength;
    this.customMaxPrefixCompletionExtraChars = customMaxPrefixCompletionExtraChars;
    this.customMaxPrefixLexiconCandidates = customMaxPrefixLexiconCandidates;
    this.customPrefixCompletionBonusScore = customPrefixCompletionBonusScore;
    this.customSourceOrderWeight = customSourceOrderWeight;
    this.panel = panel;
  }

  public JPanel panel() {
    return panel;
  }
}
