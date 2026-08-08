package cafe.woden.ircclient.notify.api.irc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class IrcEventNotificationPresetCatalogTest {

  @Test
  void buildsEssentialPresetWithExpectedEventOrderAndSourceModes() {
    List<IrcEventNotificationDefaultRule> rules =
        IrcEventNotificationPresetCatalog.buildPreset("essential", List.of("IGNORED"));

    assertEquals(5, rules.size());
    assertRule(rules.get(0), "PRIVATE_MESSAGE_RECEIVED", "OTHERS");
    assertRule(rules.get(1), "INVITE_RECEIVED", "OTHERS");
    assertRule(rules.get(2), "YOU_KICKED", "ANY");
    assertRule(rules.get(3), "YOU_BANNED", "ANY");
    assertRule(rules.get(4), "YOU_KLINED", "ANY");
  }

  @Test
  void buildsModerationPresetWithInviteAnyCompanion() {
    List<IrcEventNotificationDefaultRule> rules =
        IrcEventNotificationPresetCatalog.buildPreset(" MODERATION ", List.of());

    assertEquals(9, rules.size());
    assertRule(rules.get(0), "KICKED", "OTHERS");
    assertRule(rules.get(7), "DEHALF_OPPED", "OTHERS");
    assertRule(rules.get(8), "INVITE_RECEIVED", "ANY");
  }

  @Test
  void buildsAllEventsPresetFromSuppliedEventCatalog() {
    List<IrcEventNotificationDefaultRule> rules =
        IrcEventNotificationPresetCatalog.buildPreset(
            "all_events", List.of("private_message_received", " ", "you_banned"));

    assertEquals(2, rules.size());
    assertRule(rules.get(0), "PRIVATE_MESSAGE_RECEIVED", "ANY");
    assertRule(rules.get(1), "YOU_BANNED", "ANY");
  }

  @Test
  void unknownPresetBuildsEmptyList() {
    assertTrue(
        IrcEventNotificationPresetCatalog.buildPreset(null, List.of("YOU_BANNED")).isEmpty());
    assertTrue(
        IrcEventNotificationPresetCatalog.buildPreset("not-a-preset", List.of("YOU_BANNED"))
            .isEmpty());
  }

  private static void assertRule(
      IrcEventNotificationDefaultRule rule, String eventType, String sourceMode) {
    assertTrue(rule.enabled());
    assertEquals(eventType, rule.eventType());
    assertEquals(sourceMode, rule.sourceMode());
    assertEquals("ALL", rule.channelScope());
    assertTrue(rule.toastEnabled());
    assertEquals("BACKGROUND_ONLY", rule.focusScope());
    assertTrue(rule.statusBarEnabled());
    assertTrue(rule.notificationsNodeEnabled());
    assertFalse(rule.soundEnabled());
    assertEquals(
        IrcEventNotificationDefaultRuleCatalog.defaultBuiltInSoundIdForEvent(eventType),
        rule.soundId());
  }
}
