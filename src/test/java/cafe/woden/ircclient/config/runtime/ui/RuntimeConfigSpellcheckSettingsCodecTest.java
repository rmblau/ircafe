package cafe.woden.ircclient.config.runtime.ui;

import static cafe.woden.ircclient.config.runtime.ui.RuntimeConfigSpellcheckSettingsCodec.normalizeCompletionPreset;
import static cafe.woden.ircclient.config.runtime.ui.RuntimeConfigSpellcheckSettingsCodec.normalizeCustomMaxPrefixCompletionExtraChars;
import static cafe.woden.ircclient.config.runtime.ui.RuntimeConfigSpellcheckSettingsCodec.normalizeCustomMaxPrefixLexiconCandidates;
import static cafe.woden.ircclient.config.runtime.ui.RuntimeConfigSpellcheckSettingsCodec.normalizeCustomMinPrefixCompletionTokenLength;
import static cafe.woden.ircclient.config.runtime.ui.RuntimeConfigSpellcheckSettingsCodec.normalizeCustomPrefixCompletionBonusScore;
import static cafe.woden.ircclient.config.runtime.ui.RuntimeConfigSpellcheckSettingsCodec.normalizeCustomSourceOrderWeight;
import static cafe.woden.ircclient.config.runtime.ui.RuntimeConfigSpellcheckSettingsCodec.normalizeLanguageTag;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class RuntimeConfigSpellcheckSettingsCodecTest {

  @Test
  void normalizePresetAndLanguageTagUseUiPropertyRules() {
    assertEquals("conservative", normalizeCompletionPreset(" Conservative "));
    assertEquals("en-GB", normalizeLanguageTag("en_gb"));
  }

  @Test
  void normalizeCustomCompletionBounds() {
    assertEquals(2, normalizeCustomMinPrefixCompletionTokenLength(1));
    assertEquals(6, normalizeCustomMinPrefixCompletionTokenLength(99));
    assertEquals(4, normalizeCustomMaxPrefixCompletionExtraChars(1));
    assertEquals(24, normalizeCustomMaxPrefixCompletionExtraChars(99));
    assertEquals(16, normalizeCustomMaxPrefixLexiconCandidates(1));
    assertEquals(256, normalizeCustomMaxPrefixLexiconCandidates(999));
    assertEquals(0, normalizeCustomPrefixCompletionBonusScore(-1));
    assertEquals(400, normalizeCustomPrefixCompletionBonusScore(999));
    assertEquals(0, normalizeCustomSourceOrderWeight(-1));
    assertEquals(20, normalizeCustomSourceOrderWeight(99));
  }
}
