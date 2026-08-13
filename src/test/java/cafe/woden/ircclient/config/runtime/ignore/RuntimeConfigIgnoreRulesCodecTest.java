package cafe.woden.ircclient.config.runtime.ignore;

import static cafe.woden.ircclient.config.runtime.ignore.RuntimeConfigIgnoreRulesCodec.normalizeIgnoreChannels;
import static cafe.woden.ircclient.config.runtime.ignore.RuntimeConfigIgnoreRulesCodec.normalizeIgnoreLevels;
import static cafe.woden.ircclient.config.runtime.ignore.RuntimeConfigIgnoreRulesCodec.normalizeIgnorePatternMode;
import static cafe.woden.ircclient.config.runtime.ignore.RuntimeConfigIgnoreRulesCodec.putHardIgnoreMask;
import static cafe.woden.ircclient.config.runtime.ignore.RuntimeConfigIgnoreRulesCodec.putIgnoreMaskChannels;
import static cafe.woden.ircclient.config.runtime.ignore.RuntimeConfigIgnoreRulesCodec.putIgnoreMaskExpiresAt;
import static cafe.woden.ircclient.config.runtime.ignore.RuntimeConfigIgnoreRulesCodec.putIgnoreMaskLevels;
import static cafe.woden.ircclient.config.runtime.ignore.RuntimeConfigIgnoreRulesCodec.putIgnoreMaskPattern;
import static cafe.woden.ircclient.config.runtime.ignore.RuntimeConfigIgnoreRulesCodec.putIgnoreMaskReplies;
import static cafe.woden.ircclient.config.runtime.ignore.RuntimeConfigIgnoreRulesCodec.putSoftIgnoreMask;
import static cafe.woden.ircclient.config.runtime.ignore.RuntimeConfigIgnoreRulesCodec.removeHardIgnoreMask;
import static cafe.woden.ircclient.config.runtime.ignore.RuntimeConfigIgnoreRulesCodec.removeSoftIgnoreMask;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RuntimeConfigIgnoreRulesCodecTest {

  @Test
  void normalizeIgnoreLevelsFiltersUnknownValuesDeduplicatesAndDefaultsToAll() {
    assertEquals(
        List.of("ALL", "MSGS", "NOTICES", "CTCPS"),
        normalizeIgnoreLevels(List.of("*", "+msgs", "-NOTICES", "ctcps", "unknown", "MSGS")));
    assertEquals(List.of("ALL"), normalizeIgnoreLevels(List.of("", "unknown")));
    assertEquals(List.of("ALL"), normalizeIgnoreLevels(null));
  }

  @Test
  void normalizeIgnoreChannelsKeepsOnlyChannelScopesAndDeduplicates() {
    assertEquals(
        List.of("#ircafe", "&ops", "#IRCAFE"),
        normalizeIgnoreChannels(List.of(" #ircafe ", "nick", "&ops", "", "#IRCAFE")));
    assertEquals(List.of(), normalizeIgnoreChannels(List.of("", "nick")));
    assertEquals(List.of(), normalizeIgnoreChannels(null));
  }

  @Test
  void normalizeIgnorePatternModeUsesGlobUnlessRegexpOrFullIsExplicit() {
    assertEquals("regexp", normalizeIgnorePatternMode("regex"));
    assertEquals("regexp", normalizeIgnorePatternMode(" regexp "));
    assertEquals("full", normalizeIgnorePatternMode("FULL"));
    assertEquals("glob", normalizeIgnorePatternMode("glob"));
    assertEquals("glob", normalizeIgnorePatternMode("unknown"));
    assertEquals("glob", normalizeIgnorePatternMode(null));
  }

  @Test
  void putHardIgnoreMaskDeduplicatesCaseInsensitively() {
    Map<String, Object> server = new LinkedHashMap<>();

    putHardIgnoreMask(server, "BadNick!*@*");
    putHardIgnoreMask(server, "badnick!*@*");

    assertEquals(List.of("BadNick!*@*"), server.get("masks"));
  }

  @Test
  void putIgnoreMaskMetadataNormalizesDefaultsAndPersistedKeys() {
    Map<String, Object> server = new LinkedHashMap<>();

    putIgnoreMaskLevels(server, "BadNick!*@*", List.of("+MSGS", "-notices", "unknown"));
    putIgnoreMaskChannels(server, "BadNick!*@*", List.of(" #ircafe ", "nick"));
    putIgnoreMaskExpiresAt(server, "BadNick!*@*", 123L);
    putIgnoreMaskPattern(server, "BadNick!*@*", " afk* ", "regex");
    putIgnoreMaskReplies(server, "BadNick!*@*", true);

    assertEquals(Map.of("[BadNick!*@*]", List.of("MSGS", "NOTICES")), server.get("maskLevels"));
    assertEquals(Map.of("[BadNick!*@*]", List.of("#ircafe")), server.get("maskChannels"));
    assertEquals(Map.of("[BadNick!*@*]", 123L), server.get("maskExpiresAt"));
    assertEquals(Map.of("[BadNick!*@*]", "afk*"), server.get("maskPatterns"));
    assertEquals(Map.of("[BadNick!*@*]", "regexp"), server.get("maskPatternModes"));
    assertEquals(Map.of("[BadNick!*@*]", true), server.get("maskReplies"));
  }

  @Test
  void putIgnoreMaskMetadataPrunesDefaultAndBlankValues() {
    Map<String, Object> server = new LinkedHashMap<>();

    putIgnoreMaskLevels(server, "Nick", List.of("MSGS"));
    putIgnoreMaskChannels(server, "Nick", List.of("#ircafe"));
    putIgnoreMaskExpiresAt(server, "Nick", 123L);
    putIgnoreMaskPattern(server, "Nick", "afk", "regex");
    putIgnoreMaskReplies(server, "Nick", true);

    putIgnoreMaskLevels(server, "nick", List.of("*"));
    putIgnoreMaskChannels(server, "nick", List.of("not-a-channel"));
    putIgnoreMaskExpiresAt(server, "nick", 0L);
    putIgnoreMaskPattern(server, "nick", "", "regex");
    putIgnoreMaskReplies(server, "nick", false);

    assertFalse(server.containsKey("maskLevels"));
    assertFalse(server.containsKey("maskChannels"));
    assertFalse(server.containsKey("maskExpiresAt"));
    assertFalse(server.containsKey("maskPatterns"));
    assertFalse(server.containsKey("maskPatternModes"));
    assertFalse(server.containsKey("maskReplies"));
  }

  @Test
  void putIgnoreMaskPatternOmitsDefaultGlobMode() {
    Map<String, Object> server = new LinkedHashMap<>();

    putIgnoreMaskPattern(server, "Nick", "status*", "glob");

    assertEquals(Map.of("Nick", "status*"), server.get("maskPatterns"));
    assertFalse(server.containsKey("maskPatternModes"));
  }

  @Test
  void removeHardIgnoreMaskRemovesMaskAndAllScopedMetadata() {
    Map<String, Object> server = new LinkedHashMap<>();
    putHardIgnoreMask(server, "BadNick!*@*");
    putIgnoreMaskLevels(server, "BadNick!*@*", List.of("MSGS"));
    putIgnoreMaskChannels(server, "BadNick!*@*", List.of("#ircafe"));
    putIgnoreMaskExpiresAt(server, "BadNick!*@*", 123L);
    putIgnoreMaskPattern(server, "BadNick!*@*", "afk", "full");
    putIgnoreMaskReplies(server, "BadNick!*@*", true);

    assertTrue(removeHardIgnoreMask(server, "badnick!*@*"));

    assertFalse(server.containsKey("masks"));
    assertFalse(server.containsKey("maskLevels"));
    assertFalse(server.containsKey("maskChannels"));
    assertFalse(server.containsKey("maskExpiresAt"));
    assertFalse(server.containsKey("maskPatterns"));
    assertFalse(server.containsKey("maskPatternModes"));
    assertFalse(server.containsKey("maskReplies"));
    assertFalse(removeHardIgnoreMask(server, "badnick!*@*"));
  }

  @Test
  void softIgnoreMaskMutationDeduplicatesAndRemovesCaseInsensitively() {
    Map<String, Object> server = new LinkedHashMap<>();

    putSoftIgnoreMask(server, "Quiet!*@*");
    putSoftIgnoreMask(server, "quiet!*@*");

    assertEquals(List.of("Quiet!*@*"), server.get("softMasks"));
    assertTrue(removeSoftIgnoreMask(server, "QUIET!*@*"));
    assertFalse(server.containsKey("softMasks"));
    assertFalse(removeSoftIgnoreMask(server, "QUIET!*@*"));
  }
}
