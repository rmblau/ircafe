package cafe.woden.ircclient.config.runtime.ircv3;

import static cafe.woden.ircclient.config.runtime.ircv3.RuntimeConfigIrcv3StsPolicyCodec.hostKeysMatch;
import static cafe.woden.ircclient.config.runtime.ircv3.RuntimeConfigIrcv3StsPolicyCodec.normalizeHostKey;
import static cafe.woden.ircclient.config.runtime.ircv3.RuntimeConfigIrcv3StsPolicyCodec.normalizePolicy;
import static cafe.woden.ircclient.config.runtime.ircv3.RuntimeConfigIrcv3StsPolicyCodec.parsePolicies;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.config.api.Ircv3StsPolicyConfigPort;
import cafe.woden.ircclient.config.runtime.ircv3.RuntimeConfigIrcv3StsPolicyCodec.StsPolicyPersistence;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RuntimeConfigIrcv3StsPolicyCodecTest {

  @Test
  void parsePoliciesNormalizesHostsAndSkipsInvalidEntries() {
    Map<String, Object> raw = new LinkedHashMap<>();
    raw.put(
        " IRC.Example.NET ",
        Map.of(
            "expiresAtEpochMs",
            1_900_000_000_000L,
            "durationSeconds",
            -1L,
            "port",
            70_000,
            "preload",
            true,
            "rawValue",
            " duration=86400 "));
    raw.put("missing-expiry.example", Map.of("durationSeconds", 60L));
    raw.put(" ", Map.of("expiresAtEpochMs", 1L));
    raw.put("not-a-map.example", "bad");

    Map<String, Ircv3StsPolicyConfigPort.StsPolicySnapshot> parsed = parsePolicies(raw);

    assertEquals(1, parsed.size());
    Ircv3StsPolicyConfigPort.StsPolicySnapshot snapshot = parsed.get("irc.example.net");
    assertEquals(1_900_000_000_000L, snapshot.expiresAtEpochMs());
    assertNull(snapshot.port());
    assertTrue(snapshot.preload());
    assertEquals(0L, snapshot.durationSeconds());
    assertEquals("duration=86400", snapshot.rawValue());
  }

  @Test
  void normalizePolicyRejectsInvalidWriteInputsAndBuildsYamlMap() {
    assertNull(normalizePolicy(0L, 6697, false, 86_400L, "raw"));
    assertNull(normalizePolicy(1_900_000_000_000L, 6697, false, 0L, "raw"));

    StsPolicyPersistence policy =
        normalizePolicy(1_900_000_000_000L, 0, true, 86_400L, " duration=86400 ");

    assertEquals(1_900_000_000_000L, policy.expiresAtEpochMs());
    assertNull(policy.port());
    assertTrue(policy.preload());
    assertEquals(86_400L, policy.durationSeconds());
    assertEquals("duration=86400", policy.rawValue());
    assertEquals(
        Map.of(
            "expiresAtEpochMs",
            1_900_000_000_000L,
            "durationSeconds",
            86_400L,
            "preload",
            true,
            "rawValue",
            "duration=86400"),
        policy.toYamlMap());
  }

  @Test
  void hostHelpersNormalizeAndCompareKeys() {
    assertEquals("irc.example.net", normalizeHostKey(" IRC.Example.NET "));
    assertNull(normalizeHostKey(" "));
    assertTrue(hostKeysMatch("IRC.Example.NET", " irc.example.net "));
    assertFalse(hostKeysMatch(" ", "irc.example.net"));
  }
}
