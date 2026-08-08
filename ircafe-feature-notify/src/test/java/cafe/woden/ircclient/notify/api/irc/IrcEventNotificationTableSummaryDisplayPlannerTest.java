package cafe.woden.ircclient.notify.api.irc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class IrcEventNotificationTableSummaryDisplayPlannerTest {

  @Test
  void trimsAndBoundsSourceChannelCtcpAndScriptValues() {
    IrcEventNotificationMatchSummaryPlan match =
        new IrcEventNotificationMatchSummaryPlan(
            "GLOB",
            true,
            "  " + "a".repeat(70) + "  ",
            "ONLY",
            true,
            "  " + "#".repeat(70) + "  ",
            true,
            "LIKE",
            true,
            "  " + "c".repeat(40) + "  ",
            "LIKE",
            true,
            "  " + "v".repeat(40) + "  ");
    IrcEventNotificationActionSummaryPlan action =
        new IrcEventNotificationActionSummaryPlan(
            false,
            null,
            false,
            false,
            false,
            false,
            null,
            true,
            "/opt/scripts/" + "notify".repeat(8) + ".sh");

    IrcEventNotificationTableSummaryDisplayPlan plan =
        IrcEventNotificationTableSummaryDisplayPlanner.plan(match, action);

    assertEquals(56, plan.sourcePattern().length());
    assertEquals("…", plan.sourcePattern().substring(55));
    assertEquals(56, plan.channelPatterns().length());
    assertEquals("…", plan.channelPatterns().substring(55));
    assertEquals(24, plan.ctcpCommandPattern().length());
    assertEquals("…", plan.ctcpCommandPattern().substring(23));
    assertEquals(24, plan.ctcpValuePattern().length());
    assertEquals("…", plan.ctcpValuePattern().substring(23));
    assertEquals(26, plan.scriptLeafName().length());
    assertEquals("…", plan.scriptLeafName().substring(25));
  }

  @Test
  void blankAndNullValuesStayNullForRootFallbacks() {
    IrcEventNotificationMatchSummaryPlan match =
        new IrcEventNotificationMatchSummaryPlan(
            "GLOB", true, " ", "ONLY", true, null, true, "LIKE", true, "", "LIKE", true, " ");
    IrcEventNotificationActionSummaryPlan action =
        new IrcEventNotificationActionSummaryPlan(
            false, null, false, false, false, false, null, true, " ");

    IrcEventNotificationTableSummaryDisplayPlan plan =
        IrcEventNotificationTableSummaryDisplayPlanner.plan(match, action);

    assertNull(plan.sourcePattern());
    assertNull(plan.channelPatterns());
    assertNull(plan.ctcpCommandPattern());
    assertNull(plan.ctcpValuePattern());
    assertNull(plan.scriptLeafName());
  }

  @Test
  void boundedHandlesSmallLimits() {
    assertEquals("", IrcEventNotificationTableSummaryDisplayPlanner.bounded("abc", 0));
    assertEquals("…", IrcEventNotificationTableSummaryDisplayPlanner.bounded("abc", 1));
    assertEquals("a…", IrcEventNotificationTableSummaryDisplayPlanner.bounded("abc", 2));
    assertEquals("abc", IrcEventNotificationTableSummaryDisplayPlanner.bounded("abc", 3));
  }
}
