package cafe.woden.ircclient.notify.api.store;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class NotificationRuleCooldownPolicyTest {

  @Test
  void normalizesConfiguredCooldownSeconds() {
    assertEquals(
        NotificationRuleCooldownPolicy.DEFAULT_COOLDOWN_SECONDS,
        NotificationRuleCooldownPolicy.normalizeCooldownSeconds(-1));
    assertEquals(0, NotificationRuleCooldownPolicy.normalizeCooldownSeconds(0));
    assertEquals(3600, NotificationRuleCooldownPolicy.normalizeCooldownSeconds(5000));
  }

  @Test
  void clampsCustomFallbackForNegativeConfiguredValues() {
    assertEquals(0, NotificationRuleCooldownPolicy.normalizeCooldownSeconds(-1, -5));
    assertEquals(3600, NotificationRuleCooldownPolicy.normalizeCooldownSeconds(-1, 5000));
    assertEquals(42, NotificationRuleCooldownPolicy.normalizeCooldownSeconds(-1, 42));
  }
}
