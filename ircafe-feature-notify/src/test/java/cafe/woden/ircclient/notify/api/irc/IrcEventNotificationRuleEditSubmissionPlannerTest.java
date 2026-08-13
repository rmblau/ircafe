package cafe.woden.ircclient.notify.api.irc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class IrcEventNotificationRuleEditSubmissionPlannerTest {

  @Test
  void trimsAndKeepsActivePatternValues() {
    IrcEventNotificationRuleEditSubmissionPlan plan =
        IrcEventNotificationRuleEditSubmissionPlanner.plan(
            " ctcp_received ",
            " glob ",
            "  Alice*  ",
            " only ",
            "  #ops  ",
            " like ",
            "  VERSION  ",
            " regex ",
            "  ^client  ",
            true,
            " /tmp/sound.wav ",
            true,
            " /tmp/run.sh ",
            "  --flag  ",
            " /tmp ");

    assertEquals("CTCP_RECEIVED", plan.eventType());
    assertEquals("GLOB", plan.sourceMode());
    assertEquals("Alice*", plan.sourcePattern());
    assertEquals("ONLY", plan.channelScope());
    assertEquals("#ops", plan.channelPatterns());
    assertEquals("LIKE", plan.ctcpCommandMode());
    assertEquals("VERSION", plan.ctcpCommandPattern());
    assertEquals("REGEX", plan.ctcpValueMode());
    assertEquals("^client", plan.ctcpValuePattern());
    assertTrue(plan.soundUseCustom());
    assertEquals("/tmp/sound.wav", plan.soundCustomPath());
    assertTrue(plan.scriptEnabled());
    assertEquals("/tmp/run.sh", plan.scriptPath());
    assertEquals("--flag", plan.scriptArgs());
    assertEquals("/tmp", plan.scriptWorkingDirectory());
  }

  @Test
  void clearsInactivePatternsAndDisabledCustomActions() {
    IrcEventNotificationRuleEditSubmissionPlan plan =
        IrcEventNotificationRuleEditSubmissionPlanner.plan(
            "invite_received",
            "any",
            "  ignored  ",
            "all",
            "  #ignored  ",
            "regex",
            "  ignored  ",
            "like",
            "  ignored  ",
            true,
            " ",
            true,
            " ",
            "  ",
            "  ");

    assertEquals("INVITE_RECEIVED", plan.eventType());
    assertNull(plan.sourcePattern());
    assertNull(plan.channelPatterns());
    assertEquals("ANY", plan.ctcpCommandMode());
    assertNull(plan.ctcpCommandPattern());
    assertEquals("ANY", plan.ctcpValueMode());
    assertNull(plan.ctcpValuePattern());
    assertFalse(plan.soundUseCustom());
    assertNull(plan.soundCustomPath());
    assertFalse(plan.scriptEnabled());
    assertNull(plan.scriptPath());
    assertNull(plan.scriptArgs());
    assertNull(plan.scriptWorkingDirectory());
  }

  @Test
  void clearsAnyCtcpModePatternsForCtcpEvents() {
    IrcEventNotificationRuleEditSubmissionPlan plan =
        IrcEventNotificationRuleEditSubmissionPlanner.plan(
            "CTCP_RECEIVED",
            "ANY",
            null,
            "ALL",
            null,
            "ANY",
            " ignored ",
            "ANY",
            " ignored ",
            false,
            null,
            false,
            null,
            null,
            null);

    assertEquals("ANY", plan.ctcpCommandMode());
    assertNull(plan.ctcpCommandPattern());
    assertEquals("ANY", plan.ctcpValueMode());
    assertNull(plan.ctcpValuePattern());
  }
}
