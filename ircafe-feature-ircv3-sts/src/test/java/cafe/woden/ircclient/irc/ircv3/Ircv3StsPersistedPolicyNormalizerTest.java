package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class Ircv3StsPersistedPolicyNormalizerTest {

  private final Ircv3StsPersistedPolicyNormalizer normalizer =
      new Ircv3StsPersistedPolicyNormalizer();

  @Test
  void restoresActiveSnapshotAndRepairsLegacyDurationAndPort() {
    var result =
        normalizer.normalize(
            "IRC.Example.NET",
            new Ircv3StsPersistedPolicyNormalizer.Snapshot(
                11_000L, 70_000, true, 0L, "duration=10,preload"),
            1_000L);

    Ircv3StsPolicy policy = result.policy().orElseThrow();
    assertEquals("irc.example.net", result.hostLower());
    assertEquals(null, policy.port());
    assertEquals(10L, policy.durationSeconds());
    assertTrue(policy.preload());
    assertFalse(result.forgetPersisted());
  }

  @Test
  void expiredSnapshotRequestsPersistentRemoval() {
    var result =
        normalizer.normalize(
            "irc.example.net",
            new Ircv3StsPersistedPolicyNormalizer.Snapshot(1_000L, 6697, false, 60L, "duration=60"),
            1_000L);

    assertTrue(result.policy().isEmpty());
    assertTrue(result.forgetPersisted());
  }
}
