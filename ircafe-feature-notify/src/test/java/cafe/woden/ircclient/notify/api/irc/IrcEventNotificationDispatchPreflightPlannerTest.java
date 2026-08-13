package cafe.woden.ircclient.notify.api.irc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class IrcEventNotificationDispatchPreflightPlannerTest {

  @Test
  void acceptsEventTypeAndPositiveRuleCount() {
    IrcEventNotificationDispatchPreflightPlan plan =
        IrcEventNotificationDispatchPreflightPlanner.plan("  MESSAGE_RECEIVED  ", 2);

    assertTrue(plan.shouldEvaluate());
    assertEquals("MESSAGE_RECEIVED", plan.eventTypeName());
    assertEquals(2, plan.ruleCount());
  }

  @Test
  void rejectsBlankEventTypeOrEmptyRules() {
    assertFalse(IrcEventNotificationDispatchPreflightPlanner.plan(" ", 2).shouldEvaluate());
    assertFalse(IrcEventNotificationDispatchPreflightPlanner.plan(null, 2).shouldEvaluate());
    assertFalse(
        IrcEventNotificationDispatchPreflightPlanner.plan("MESSAGE_RECEIVED", 0).shouldEvaluate());
    assertFalse(
        IrcEventNotificationDispatchPreflightPlanner.plan("MESSAGE_RECEIVED", -2).shouldEvaluate());
  }

  @Test
  void validatesMatchedRuleIndexesAgainstRuleCount() {
    IrcEventNotificationDispatchPreflightPlan plan =
        IrcEventNotificationDispatchPreflightPlanner.plan("MESSAGE_RECEIVED", 2);

    assertTrue(plan.matchedRuleIndexValid(0));
    assertTrue(plan.matchedRuleIndexValid(1));
    assertFalse(plan.matchedRuleIndexValid(null));
    assertFalse(plan.matchedRuleIndexValid(-1));
    assertFalse(plan.matchedRuleIndexValid(2));
  }
}
