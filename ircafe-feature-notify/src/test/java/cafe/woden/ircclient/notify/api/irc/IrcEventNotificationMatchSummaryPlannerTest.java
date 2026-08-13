package cafe.woden.ircclient.notify.api.irc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class IrcEventNotificationMatchSummaryPlannerTest {

  @Test
  void plansSourceChannelAndCtcpPatternRequirements() {
    IrcEventNotificationMatchSummaryPlan plan =
        IrcEventNotificationMatchSummaryPlanner.plan(
            new IrcEventNotificationMatchRule(
                true,
                " ctcp_received ",
                " glob ",
                "  Alice*  ",
                " only ",
                "  #ops,#dev  ",
                " like ",
                "  VERSION  ",
                " regex ",
                "  .+client.+  "));

    assertEquals("GLOB", plan.sourceMode());
    assertTrue(plan.sourcePatternRequired());
    assertEquals("Alice*", plan.sourcePattern());
    assertEquals("ONLY", plan.channelScope());
    assertTrue(plan.channelPatternsRequired());
    assertEquals("#ops,#dev", plan.channelPatterns());
    assertTrue(plan.ctcpFiltersActive());
    assertEquals("LIKE", plan.ctcpCommandMode());
    assertTrue(plan.ctcpCommandPatternRequired());
    assertEquals("VERSION", plan.ctcpCommandPattern());
    assertEquals("REGEX", plan.ctcpValueMode());
    assertTrue(plan.ctcpValuePatternRequired());
    assertEquals(".+client.+", plan.ctcpValuePattern());
  }

  @Test
  void clearsInactivePatternsForSummaryOutput() {
    IrcEventNotificationMatchSummaryPlan plan =
        IrcEventNotificationMatchSummaryPlanner.plan(
            new IrcEventNotificationMatchRule(
                true,
                "INVITE_RECEIVED",
                "ANY",
                "ignored source",
                "ALL",
                "#ignored",
                "ANY",
                "ignored command",
                "ANY",
                "ignored value"));

    assertFalse(plan.sourcePatternRequired());
    assertNull(plan.sourcePattern());
    assertFalse(plan.channelPatternsRequired());
    assertNull(plan.channelPatterns());
    assertFalse(plan.ctcpCommandPatternRequired());
    assertNull(plan.ctcpCommandPattern());
    assertFalse(plan.ctcpValuePatternRequired());
    assertNull(plan.ctcpValuePattern());
  }

  @Test
  void disablesCtcpModesAndPatternsForNonCtcpEvents() {
    IrcEventNotificationMatchSummaryPlan plan =
        IrcEventNotificationMatchSummaryPlanner.plan(
            new IrcEventNotificationMatchRule(
                true,
                "TOPIC_CHANGED",
                "ANY",
                null,
                "ALL",
                null,
                "LIKE",
                "VERSION",
                "LIKE",
                "client"));

    assertFalse(plan.ctcpFiltersActive());
    assertEquals("ANY", plan.ctcpCommandMode());
    assertFalse(plan.ctcpCommandPatternRequired());
    assertNull(plan.ctcpCommandPattern());
    assertEquals("ANY", plan.ctcpValueMode());
    assertFalse(plan.ctcpValuePatternRequired());
    assertNull(plan.ctcpValuePattern());
  }

  @Test
  void nullRuleProducesEmptySummaryPlan() {
    IrcEventNotificationMatchSummaryPlan plan = IrcEventNotificationMatchSummaryPlanner.plan(null);

    assertEquals("ANY", plan.sourceMode());
    assertFalse(plan.sourcePatternRequired());
    assertNull(plan.sourcePattern());
    assertEquals("ALL", plan.channelScope());
    assertFalse(plan.channelPatternsRequired());
    assertNull(plan.channelPatterns());
    assertFalse(plan.ctcpFiltersActive());
    assertEquals("ANY", plan.ctcpCommandMode());
    assertFalse(plan.ctcpCommandPatternRequired());
    assertNull(plan.ctcpCommandPattern());
    assertEquals("ANY", plan.ctcpValueMode());
    assertFalse(plan.ctcpValuePatternRequired());
    assertNull(plan.ctcpValuePattern());
  }
}
