package cafe.woden.ircclient.config.runtime.ui;

import static cafe.woden.ircclient.config.runtime.ui.RuntimeConfigFilterSettingsCodec.ScalarSetting.ENABLED_BY_DEFAULT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import cafe.woden.ircclient.model.FilterAction;
import cafe.woden.ircclient.model.FilterDirection;
import cafe.woden.ircclient.model.FilterRule;
import cafe.woden.ircclient.model.FilterScopeOverride;
import cafe.woden.ircclient.model.LogKind;
import cafe.woden.ircclient.model.RegexFlag;
import cafe.woden.ircclient.model.RegexSpec;
import cafe.woden.ircclient.model.TagSpec;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RuntimeConfigFilterSettingsCodecTest {

  @Test
  void scalarSettingsExposeStableKeysAndDescriptions() {
    assertEquals("enabledByDefault", ENABLED_BY_DEFAULT.key());
    assertEquals("filters enabledByDefault setting", ENABLED_BY_DEFAULT.description());
  }

  @Test
  void placeholderRangesAreNormalizedBeforePersistence() {
    assertEquals(25, RuntimeConfigFilterSettingsCodec.normalizePlaceholderMaxPreviewLines(99));
    assertEquals(
        50_000, RuntimeConfigFilterSettingsCodec.normalizePlaceholderMaxLinesPerRun(100_000));
    assertEquals(500, RuntimeConfigFilterSettingsCodec.normalizePlaceholderTooltipMaxTags(700));
    assertEquals(
        5_000, RuntimeConfigFilterSettingsCodec.normalizeHistoryPlaceholderMaxRunsPerBatch(8_000));
  }

  @Test
  @SuppressWarnings("unchecked")
  void serializeRulesNormalizesOptionalFields() {
    List<Map<String, Object>> serialized =
        RuntimeConfigFilterSettingsCodec.serializeRules(
            Arrays.asList(
                new FilterRule(
                    null,
                    " noise ",
                    false,
                    " libera/#chan ",
                    FilterAction.DIM,
                    FilterDirection.IN,
                    EnumSet.of(LogKind.CHAT),
                    Arrays.asList(" spammer ", ""),
                    new RegexSpec("buy now", EnumSet.of(RegexFlag.M, RegexFlag.I)),
                    TagSpec.parse(" irc_privmsg ")),
                null));

    Map<String, Object> rule = serialized.getFirst();
    assertEquals("noise", rule.get("name"));
    assertEquals(false, rule.get("enabled"));
    assertEquals("libera/#chan", rule.get("scope"));
    assertEquals("DIM", rule.get("action"));
    assertEquals("IN", rule.get("dir"));
    assertEquals(List.of("CHAT"), rule.get("kinds"));
    assertEquals(List.of("spammer"), rule.get("from"));
    assertEquals("irc_privmsg", rule.get("tags"));
    assertEquals("im", ((Map<String, Object>) rule.get("text")).get("flags"));
  }

  @Test
  void serializeRulesUsesDefaultsForMissingActionAndDirection() {
    Map<String, Object> rule =
        RuntimeConfigFilterSettingsCodec.serializeRules(
                List.of(new FilterRule(null, null, true, null, null, null, null, null, null, null)))
            .getFirst();

    assertEquals("", rule.get("name"));
    assertEquals("*", rule.get("scope"));
    assertEquals("HIDE", rule.get("action"));
    assertEquals("ANY", rule.get("dir"));
    assertFalse(rule.containsKey("kinds"));
    assertFalse(rule.containsKey("from"));
    assertFalse(rule.containsKey("tags"));
    assertFalse(rule.containsKey("text"));
  }

  @Test
  void serializeOverridesOmitsNullOptions() {
    Map<String, Object> override =
        RuntimeConfigFilterSettingsCodec.serializeOverrides(
                Arrays.asList(new FilterScopeOverride(" libera/#chan ", false, true, null), null))
            .getFirst();

    assertEquals("libera/#chan", override.get("scope"));
    assertEquals(false, override.get("filtersEnabled"));
    assertEquals(true, override.get("placeholdersEnabled"));
    assertFalse(override.containsKey("placeholdersCollapsed"));
  }
}
