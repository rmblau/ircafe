package cafe.woden.ircclient.notify.api.irc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class IrcEventNotificationActionSummaryPlannerTest {

  @Test
  void summarizesNormalizedActionPlan() {
    IrcEventNotificationActionSummaryPlan plan =
        IrcEventNotificationActionSummaryPlanner.plan(
            new IrcEventNotificationActionRule(
                true,
                true,
                " BACKGROUND_ONLY ",
                true,
                true,
                " NOTIF_3 ",
                true,
                " sounds/custom.wav ",
                true,
                " /usr/local/bin/notify ",
                " --quiet ",
                " /tmp "));

    assertTrue(plan.toastEnabled());
    assertEquals("BACKGROUND_ONLY", plan.focusScope());
    assertTrue(plan.statusBarEnabled());
    assertTrue(plan.notificationsNodeEnabled());
    assertTrue(plan.soundEnabled());
    assertTrue(plan.customSound());
    assertEquals("NOTIF_3", plan.soundId());
    assertTrue(plan.scriptEnabled());
    assertEquals("/usr/local/bin/notify", plan.scriptPath());
    assertEquals("notify", plan.scriptLeafName());
  }

  @Test
  void scriptLeafNameHandlesWindowsPathsAndTrailingSeparators() {
    IrcEventNotificationActionSummaryPlan windowsPath =
        new IrcEventNotificationActionSummaryPlan(
            false, null, false, false, false, false, null, true, " C:\\tools\\notify.ps1 ");
    IrcEventNotificationActionSummaryPlan trailingSeparator =
        new IrcEventNotificationActionSummaryPlan(
            false, null, false, false, false, false, null, true, "/opt/notify/");

    assertEquals("notify.ps1", windowsPath.scriptLeafName());
    assertEquals("/opt/notify/", trailingSeparator.scriptLeafName());
  }

  @Test
  void disablesCustomSoundAndScriptWhenRequiredPathsAreBlank() {
    IrcEventNotificationActionSummaryPlan plan =
        IrcEventNotificationActionSummaryPlanner.plan(
            new IrcEventNotificationActionRule(
                false, false, null, false, true, "NOTIF_1", true, " ", true, " ", null, null));

    assertTrue(plan.soundEnabled());
    assertFalse(plan.customSound());
    assertEquals("NOTIF_1", plan.soundId());
    assertFalse(plan.scriptEnabled());
    assertNull(plan.scriptPath());
  }

  @Test
  void nullRuleHasEmptySummary() {
    IrcEventNotificationActionSummaryPlan plan = IrcEventNotificationActionSummaryPlanner.plan(null);

    assertFalse(plan.toastEnabled());
    assertFalse(plan.statusBarEnabled());
    assertFalse(plan.notificationsNodeEnabled());
    assertFalse(plan.soundEnabled());
    assertFalse(plan.customSound());
    assertFalse(plan.scriptEnabled());
    assertNull(plan.focusScope());
    assertNull(plan.soundId());
    assertNull(plan.scriptPath());
  }
}
