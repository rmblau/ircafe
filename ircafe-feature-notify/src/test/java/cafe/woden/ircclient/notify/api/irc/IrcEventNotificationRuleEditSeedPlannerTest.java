package cafe.woden.ircclient.notify.api.irc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class IrcEventNotificationRuleEditSeedPlannerTest {

  @Test
  void defaultSeedUsesFeatureOwnedIrcEventDefaults() {
    IrcEventNotificationRuleEditSeedPlan plan =
        IrcEventNotificationRuleEditSeedPlanner.defaultSeed();

    assertFalse(plan.enabled());
    assertEquals("INVITE_RECEIVED", plan.eventType());
    assertEquals("ANY", plan.sourceMode());
    assertEquals("ALL", plan.channelScope());
    assertEquals("BACKGROUND_ONLY", plan.focusScope());
    assertTrue(plan.toastEnabled());
    assertTrue(plan.statusBarEnabled());
    assertTrue(plan.notificationsNodeEnabled());
    assertFalse(plan.soundEnabled());
    assertEquals("CHANNEL_INVITE_1", plan.soundId());
    assertFalse(plan.scriptEnabled());
    assertEquals("ANY", plan.ctcpCommandMode());
    assertEquals("ANY", plan.ctcpValueMode());
  }

  @Test
  void normalizesPatternsAndDependentFlags() {
    IrcEventNotificationRuleEditSeedPlan plan =
        IrcEventNotificationRuleEditSeedPlanner.plan(
            true,
            " private_message_received ",
            " any ",
            " Alice* ",
            " all ",
            " #ops ",
            true,
            " foreground_only ",
            true,
            true,
            true,
            " ",
            true,
            "   ",
            true,
            "   ",
            " --verbose ",
            " /tmp ",
            " regex ",
            " VERSION ",
            " glob ",
            " value* ");

    assertEquals("PRIVATE_MESSAGE_RECEIVED", plan.eventType());
    assertEquals("ANY", plan.sourceMode());
    assertNull(plan.sourcePattern());
    assertEquals("ALL", plan.channelScope());
    assertNull(plan.channelPatterns());
    assertEquals("FOREGROUND_ONLY", plan.focusScope());
    assertEquals("PM_RECEIVED_1", plan.soundId());
    assertFalse(plan.soundUseCustom());
    assertNull(plan.soundCustomPath());
    assertFalse(plan.scriptEnabled());
    assertNull(plan.scriptPath());
    assertEquals("--verbose", plan.scriptArgs());
    assertEquals("/tmp", plan.scriptWorkingDirectory());
    assertEquals("ANY", plan.ctcpCommandMode());
    assertNull(plan.ctcpCommandPattern());
    assertEquals("ANY", plan.ctcpValueMode());
    assertNull(plan.ctcpValuePattern());
  }

  @Test
  void keepsCtcpFiltersOnlyForCtcpEvents() {
    IrcEventNotificationRuleEditSeedPlan plan =
        IrcEventNotificationRuleEditSeedPlanner.plan(
            true,
            "CTCP_RECEIVED",
            "GLOB",
            " Alice* ",
            "ONLY",
            " #ops ",
            true,
            "BACKGROUND_ONLY",
            true,
            true,
            true,
            "SOMEBODY_SENT_CTCP_1",
            true,
            " /sounds/ctcp.wav ",
            true,
            " /bin/notify ",
            null,
            null,
            "LIKE",
            " VERSION ",
            "REGEX",
            " 1\\.0 ");

    assertEquals("GLOB", plan.sourceMode());
    assertEquals("Alice*", plan.sourcePattern());
    assertEquals("ONLY", plan.channelScope());
    assertEquals("#ops", plan.channelPatterns());
    assertTrue(plan.soundUseCustom());
    assertEquals("/sounds/ctcp.wav", plan.soundCustomPath());
    assertTrue(plan.scriptEnabled());
    assertEquals("/bin/notify", plan.scriptPath());
    assertEquals("LIKE", plan.ctcpCommandMode());
    assertEquals("VERSION", plan.ctcpCommandPattern());
    assertEquals("REGEX", plan.ctcpValueMode());
    assertEquals("1\\.0", plan.ctcpValuePattern());
  }

  @Test
  void clearsCtcpPatternsWhenModeDoesNotRequirePattern() {
    IrcEventNotificationRuleEditSeedPlan plan =
        IrcEventNotificationRuleEditSeedPlanner.plan(
            true,
            "CTCP_RECEIVED",
            "ANY",
            null,
            "ALL",
            null,
            true,
            "BACKGROUND_ONLY",
            true,
            true,
            false,
            null,
            false,
            null,
            false,
            null,
            null,
            null,
            "ANY",
            "VERSION",
            "ANY",
            "PING");

    assertEquals("ANY", plan.ctcpCommandMode());
    assertNull(plan.ctcpCommandPattern());
    assertEquals("ANY", plan.ctcpValueMode());
    assertNull(plan.ctcpValuePattern());
  }
}
