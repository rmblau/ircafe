package cafe.woden.ircclient.notify.api.store;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class NotificationRuleMatchCooldownTest {

  @Test
  void suppressesDuplicateRuleMatchesUntilCooldownExpires() {
    NotificationRuleMatchCooldown cooldown = new NotificationRuleMatchCooldown();
    Instant now = Instant.parse("2026-07-05T12:00:00Z");

    assertTrue(cooldown.allow("Libera", "#IRCafe", "Rule A", 15, now));
    assertFalse(cooldown.allow("libera", "#ircafe", "rule a", 15, now.plusSeconds(14)));
    assertTrue(cooldown.allow("libera", "#ircafe", "rule a", 15, now.plusSeconds(15)));
  }

  @Test
  void zeroCooldownAllowsRepeatedMatches() {
    NotificationRuleMatchCooldown cooldown = new NotificationRuleMatchCooldown();
    Instant now = Instant.parse("2026-07-05T12:00:00Z");

    assertTrue(cooldown.allow("libera", "#ircafe", "Rule A", 0, now));
    assertTrue(cooldown.allow("libera", "#ircafe", "Rule A", 0, now));
  }

  @Test
  void clearRuleChannelAndServerRemoveMatchingCooldownKeys() {
    NotificationRuleMatchCooldown cooldown = new NotificationRuleMatchCooldown();
    Instant now = Instant.parse("2026-07-05T12:00:00Z");

    assertTrue(cooldown.allow("libera", "#a", "Rule A", 60, now));
    assertFalse(cooldown.allow("libera", "#a", "Rule A", 60, now.plusSeconds(1)));
    cooldown.clearRule("libera", "#a", "Rule A");
    assertTrue(cooldown.allow("libera", "#a", "Rule A", 60, now.plusSeconds(2)));

    assertTrue(cooldown.allow("libera", "#b", "Rule B", 60, now));
    cooldown.clearChannel("libera", "#a");
    assertTrue(cooldown.allow("libera", "#a", "Rule A", 60, now.plusSeconds(3)));
    assertFalse(cooldown.allow("libera", "#b", "Rule B", 60, now.plusSeconds(3)));

    cooldown.clearServer("libera");
    assertTrue(cooldown.allow("libera", "#b", "Rule B", 60, now.plusSeconds(4)));
  }

  @Test
  void prunesExpiredAndExcessKeysDuringAllowChecks() {
    NotificationRuleMatchCooldown cooldown =
        new NotificationRuleMatchCooldown(Duration.ofSeconds(1), 2);
    Instant now = Instant.parse("2026-07-05T12:00:00Z");

    assertTrue(cooldown.allow("libera", "#a", "Rule A", 60, now));
    assertTrue(cooldown.allow("libera", "#b", "Rule B", 60, now.plusMillis(100)));
    assertTrue(cooldown.allow("libera", "#c", "Rule C", 60, now.plusMillis(200)));
    assertEquals(2, cooldown.size());

    assertTrue(cooldown.allow("libera", "#fresh", "Rule Fresh", 60, now.plusSeconds(2)));
    assertEquals(1, cooldown.size());
  }

  @Test
  void clearsSelectedRuleMatchEventsForRequestedServer() {
    NotificationRuleMatchCooldown cooldown = new NotificationRuleMatchCooldown();
    Instant now = Instant.parse("2026-07-05T12:00:00Z");

    assertTrue(cooldown.allow("libera", "#a", "Rule A", 60, now));
    assertTrue(cooldown.allow("libera", "#b", "Rule B", 60, now));
    assertTrue(cooldown.allow("oftc", "#a", "Rule A", 60, now));

    cooldown.clearSelectedRuleMatches(
        " LIBERA ",
        List.of(
            NotificationStoreEventPolicy.ruleMatch(
                "libera", "#a", "bob", "Rule A", "matched", "msg-a"),
            NotificationStoreEventPolicy.ruleMatch(
                "oftc", "#a", "bob", "Rule A", "matched", "msg-o"),
            NotificationStoreEventValues.invalid()));

    assertTrue(cooldown.allow("libera", "#a", "Rule A", 60, now.plusSeconds(1)));
    assertFalse(cooldown.allow("libera", "#b", "Rule B", 60, now.plusSeconds(1)));
    assertFalse(cooldown.allow("oftc", "#a", "Rule A", 60, now.plusSeconds(1)));
  }

  @Test
  void normalizesCooldownSeconds() {
    assertEquals(15, NotificationRuleMatchCooldown.normalizeCooldownSeconds(-1, 15));
    assertEquals(0, NotificationRuleMatchCooldown.normalizeCooldownSeconds(0, 15));
    assertEquals(3_600, NotificationRuleMatchCooldown.normalizeCooldownSeconds(9_999, 15));
  }
}
