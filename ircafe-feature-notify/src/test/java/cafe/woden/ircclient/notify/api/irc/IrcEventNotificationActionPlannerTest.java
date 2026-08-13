package cafe.woden.ircclient.notify.api.irc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class IrcEventNotificationActionPlannerTest {

  @Test
  void plansAllEnabledActionsForMatchedRule() {
    IrcEventNotificationActionPlan plan =
        IrcEventNotificationActionPlanner.plan(
            new IrcEventNotificationActionRule(
                true,
                true,
                "BACKGROUND_ONLY",
                true,
                true,
                "NOTIF_3",
                true,
                "sounds/custom.wav",
                true,
                "/usr/local/bin/notify",
                "--quiet",
                "/tmp"));

    assertTrue(plan.recordNotification());
    assertTrue(plan.runScript());
    assertTrue(plan.sendPush());

    IrcEventNotificationScriptAction script = plan.scriptAction();
    assertTrue(script.enabled());
    assertEquals("/usr/local/bin/notify", script.scriptPath());
    assertEquals("--quiet", script.scriptArgs());
    assertEquals("/tmp", script.workingDirectory());

    IrcEventNotificationTrayAction tray = plan.trayAction();
    assertTrue(tray.enabled());
    assertTrue(tray.showToast());
    assertTrue(tray.showStatusBar());
    assertEquals("BACKGROUND_ONLY", tray.focusScope());
    assertTrue(tray.playSound());
    assertEquals("NOTIF_3", tray.soundId());
    assertTrue(tray.soundUseCustom());
    assertEquals("sounds/custom.wav", tray.soundCustomPath());
  }

  @Test
  void disablesTrayWhenNoTrayStatusOrSoundActionIsEnabled() {
    IrcEventNotificationActionPlan plan =
        IrcEventNotificationActionPlanner.plan(
            new IrcEventNotificationActionRule(
                true, false, "ANY", false, false, "NOTIF_1", false, null, false, null, null, null));

    assertTrue(plan.recordNotification());
    assertFalse(plan.trayAction().enabled());
    assertFalse(plan.runScript());
    assertFalse(plan.scriptAction().enabled());
    assertTrue(plan.sendPush());
  }

  @Test
  void normalizesBlankCustomSoundAndScriptValues() {
    IrcEventNotificationActionRule rule =
        new IrcEventNotificationActionRule(
            false, true, " ", false, true, " ", true, " ", true, " ", " ", " ");

    IrcEventNotificationActionPlan plan = IrcEventNotificationActionPlanner.plan(rule);

    IrcEventNotificationTrayAction tray = plan.trayAction();
    assertTrue(tray.enabled());
    assertNull(tray.focusScope());
    assertNull(tray.soundId());
    assertFalse(tray.soundUseCustom());
    assertNull(tray.soundCustomPath());
    assertFalse(plan.runScript());
    assertFalse(plan.scriptAction().enabled());
    assertNull(plan.scriptAction().scriptPath());
    assertNull(plan.scriptAction().scriptArgs());
    assertNull(plan.scriptAction().workingDirectory());
  }

  @Test
  void nullRuleDisablesAllActions() {
    IrcEventNotificationActionPlan plan = IrcEventNotificationActionPlanner.plan(null);

    assertFalse(plan.recordNotification());
    assertFalse(plan.trayAction().enabled());
    assertFalse(plan.runScript());
    assertFalse(plan.scriptAction().enabled());
    assertFalse(plan.sendPush());
  }
}
