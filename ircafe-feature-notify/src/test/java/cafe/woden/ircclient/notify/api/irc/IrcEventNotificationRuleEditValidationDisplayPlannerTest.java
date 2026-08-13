package cafe.woden.ircclient.notify.api.irc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class IrcEventNotificationRuleEditValidationDisplayPlannerTest {

  @Test
  void keepsFilterValidationErrorsOnFilterTab() {
    IrcEventNotificationRuleEditValidationDisplayPlan plan =
        IrcEventNotificationRuleEditValidationDisplayPlanner.plan(
            new IrcEventNotificationRuleEditValidationError(
                IrcEventNotificationRuleEditValidationError.Field.SOURCE_PATTERN,
                IrcEventNotificationRuleEditValidationError.Reason.REQUIRED,
                ""));

    assertEquals(0, plan.tabIndex());
  }

  @Test
  void routesScriptPathValidationErrorsToScriptTab() {
    IrcEventNotificationRuleEditValidationDisplayPlan plan =
        IrcEventNotificationRuleEditValidationDisplayPlanner.plan(
            new IrcEventNotificationRuleEditValidationError(
                IrcEventNotificationRuleEditValidationError.Field.SCRIPT_PATH,
                IrcEventNotificationRuleEditValidationError.Reason.REQUIRED,
                ""));

    assertEquals(3, plan.tabIndex());
  }

  @Test
  void nullErrorDefaultsToFilterTab() {
    assertEquals(0, IrcEventNotificationRuleEditValidationDisplayPlanner.plan(null).tabIndex());
  }
}
