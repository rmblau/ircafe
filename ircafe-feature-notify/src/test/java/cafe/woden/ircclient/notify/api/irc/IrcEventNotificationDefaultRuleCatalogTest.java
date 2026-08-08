package cafe.woden.ircclient.notify.api.irc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class IrcEventNotificationDefaultRuleCatalogTest {

  @Test
  void buildsDefaultRulesInEventOrderWithStatusBarCompanions() {
    List<IrcEventNotificationDefaultRule> defaults =
        IrcEventNotificationDefaultRuleCatalog.defaults(
            List.of("PRIVATE_MESSAGE_RECEIVED", "KICKED", "BANNED", "KLINED"));

    assertEquals(7, defaults.size());
    assertEquals("PRIVATE_MESSAGE_RECEIVED", defaults.get(0).eventType());
    assertEquals("KICKED", defaults.get(1).eventType());
    assertEquals("BANNED", defaults.get(2).eventType());
    assertEquals("KLINED", defaults.get(3).eventType());

    IrcEventNotificationDefaultRule pm = defaults.getFirst();
    assertTrue(pm.enabled());
    assertEquals("OTHERS", pm.sourceMode());
    assertTrue(pm.toastEnabled());
    assertEquals("BACKGROUND_ONLY", pm.focusScope());
    assertTrue(pm.statusBarEnabled());
    assertTrue(pm.notificationsNodeEnabled());
    assertFalse(pm.soundEnabled());
    assertEquals("PM_RECEIVED_1", pm.soundId());

    IrcEventNotificationDefaultRule kickedCompanion = defaults.get(4);
    assertEquals("KICKED", kickedCompanion.eventType());
    assertTrue(kickedCompanion.enabled());
    assertFalse(kickedCompanion.toastEnabled());
    assertEquals("ANY", kickedCompanion.focusScope());
    assertTrue(kickedCompanion.statusBarEnabled());
  }

  @Test
  void selectsEventSpecificBuiltInSoundIds() {
    assertEquals(
        "YOU_DEOPPED",
        IrcEventNotificationDefaultRuleCatalog.defaultBuiltInSoundIdForEvent("YOU_DEOPPED"));
    assertEquals(
        "SOMEBODY_SENT_CTCP_1",
        IrcEventNotificationDefaultRuleCatalog.defaultBuiltInSoundIdForEvent("ctcp_received"));
    assertEquals(
        "NETSPLIT_1",
        IrcEventNotificationDefaultRuleCatalog.defaultBuiltInSoundIdForEvent(" NETSPLIT_DETECTED "));
    assertEquals(
        IrcEventNotificationDefaultRuleCatalog.DEFAULT_SOUND_ID,
        IrcEventNotificationDefaultRuleCatalog.defaultBuiltInSoundIdForEvent("not-a-real-event"));
  }

  @Test
  void normalizesBlankAndUnknownInputsToSafeDefaults() {
    List<IrcEventNotificationDefaultRule> defaults =
        IrcEventNotificationDefaultRuleCatalog.defaults(List.of(" ", "unknown", "YOU_BANNED"));

    assertEquals(2, defaults.size());
    assertEquals("UNKNOWN", defaults.get(0).eventType());
    assertFalse(defaults.get(0).enabled());
    assertEquals("ANY", defaults.get(0).sourceMode());
    assertEquals(IrcEventNotificationDefaultRuleCatalog.DEFAULT_SOUND_ID, defaults.get(0).soundId());

    assertEquals("YOU_BANNED", defaults.get(1).eventType());
    assertTrue(defaults.get(1).enabled());
    assertEquals("ANY", defaults.get(1).sourceMode());
    assertEquals("YOU_BANNED_1", defaults.get(1).soundId());
  }
}
