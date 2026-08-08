package cafe.woden.ircclient.notify.api.irc;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class IrcEventNotificationRuleToggleSelectionPlannerTest {

  @Test
  void disablesToggleActionsWhenNoValidRowIsSelected() {
    IrcEventNotificationRuleToggleSelectionPlan noSelection =
        IrcEventNotificationRuleToggleSelectionPlanner.plan(-1, 2, false);
    IrcEventNotificationRuleToggleSelectionPlan outOfRange =
        IrcEventNotificationRuleToggleSelectionPlanner.plan(2, 2, true);

    assertFalse(noSelection.enableRuleEnabled());
    assertFalse(noSelection.disableRuleEnabled());
    assertFalse(outOfRange.enableRuleEnabled());
    assertFalse(outOfRange.disableRuleEnabled());
  }

  @Test
  void selectedDisabledRuleCanOnlyBeEnabled() {
    IrcEventNotificationRuleToggleSelectionPlan plan =
        IrcEventNotificationRuleToggleSelectionPlanner.plan(0, 1, false);

    assertTrue(plan.enableRuleEnabled());
    assertFalse(plan.disableRuleEnabled());
  }

  @Test
  void selectedEnabledRuleCanOnlyBeDisabled() {
    IrcEventNotificationRuleToggleSelectionPlan plan =
        IrcEventNotificationRuleToggleSelectionPlanner.plan(0, 1, true);

    assertFalse(plan.enableRuleEnabled());
    assertTrue(plan.disableRuleEnabled());
  }
}
