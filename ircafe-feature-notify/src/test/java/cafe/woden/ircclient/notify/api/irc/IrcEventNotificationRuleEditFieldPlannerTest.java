package cafe.woden.ircclient.notify.api.irc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.notify.api.irc.IrcEventNotificationRuleEditFieldPlan.SourcePatternHint;
import org.junit.jupiter.api.Test;

class IrcEventNotificationRuleEditFieldPlannerTest {

  @Test
  void plansSourceAndChannelPatternAvailability() {
    IrcEventNotificationRuleEditFieldPlan plan =
        IrcEventNotificationRuleEditFieldPlanner.plan(
            "INVITE_RECEIVED", " glob ", "ONLY", "ANY", "ANY", false);

    assertEquals(SourcePatternHint.GLOB, plan.sourcePatternHint());
    assertTrue(plan.sourcePatternAvailable());
    assertTrue(plan.channelPatternsAvailable());
    assertFalse(plan.ctcpFiltersAvailable());
    assertFalse(plan.scriptFieldsAvailable());
  }

  @Test
  void plansCtcpPatternAvailabilityOnlyForCtcpEvents() {
    IrcEventNotificationRuleEditFieldPlan inactive =
        IrcEventNotificationRuleEditFieldPlanner.plan(
            "INVITE_RECEIVED", "ANY", "ALL", "REGEX", "LIKE", true);
    IrcEventNotificationRuleEditFieldPlan active =
        IrcEventNotificationRuleEditFieldPlanner.plan(
            "CTCP_RECEIVED", "ANY", "ALL", "REGEX", "LIKE", true);

    assertFalse(inactive.ctcpFiltersAvailable());
    assertFalse(inactive.ctcpCommandPatternAvailable());
    assertFalse(inactive.ctcpValuePatternAvailable());
    assertTrue(active.ctcpFiltersAvailable());
    assertTrue(active.ctcpCommandPatternAvailable());
    assertTrue(active.ctcpValuePatternAvailable());
    assertTrue(active.scriptFieldsAvailable());
  }

  @Test
  void mapsSourcePatternHints() {
    assertEquals(
        SourcePatternHint.NICK_LIST,
        IrcEventNotificationRuleEditFieldPlanner.plan("", "nick_list", "", "", "", false)
            .sourcePatternHint());
    assertEquals(
        SourcePatternHint.REGEX,
        IrcEventNotificationRuleEditFieldPlanner.plan("", "regex", "", "", "", false)
            .sourcePatternHint());
    assertEquals(
        SourcePatternHint.NONE,
        IrcEventNotificationRuleEditFieldPlanner.plan("", "self", "", "", "", false)
            .sourcePatternHint());
  }
}
