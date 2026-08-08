package cafe.woden.ircclient.notify.api.irc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class IrcEventNotificationRuleListSelectionPlannerTest {

  @Test
  void presetApplySelectsPreferredRowWhenValid() {
    IrcEventNotificationRuleListSelectionPlan plan =
        IrcEventNotificationRuleListSelectionPlanner.afterPresetApply(4, 2);

    assertTrue(plan.selectRow());
    assertEquals(2, plan.row());
  }

  @Test
  void presetApplyFallsBackToFirstRowWhenPreferredRowIsInvalid() {
    IrcEventNotificationRuleListSelectionPlan negative =
        IrcEventNotificationRuleListSelectionPlanner.afterPresetApply(4, -1);
    IrcEventNotificationRuleListSelectionPlan tooLarge =
        IrcEventNotificationRuleListSelectionPlanner.afterPresetApply(4, 4);

    assertTrue(negative.selectRow());
    assertEquals(0, negative.row());
    assertTrue(tooLarge.selectRow());
    assertEquals(0, tooLarge.row());
  }

  @Test
  void presetApplyClearsSelectionWhenRuleListIsEmpty() {
    IrcEventNotificationRuleListSelectionPlan plan =
        IrcEventNotificationRuleListSelectionPlanner.afterPresetApply(0, 0);

    assertFalse(plan.selectRow());
    assertEquals(-1, plan.row());
  }

  @Test
  void defaultResetSelectsFirstRowWhenPresent() {
    IrcEventNotificationRuleListSelectionPlan plan =
        IrcEventNotificationRuleListSelectionPlanner.afterDefaultReset(3);

    assertTrue(plan.selectRow());
    assertEquals(0, plan.row());
  }

  @Test
  void defaultResetClearsSelectionWhenRuleListIsEmpty() {
    IrcEventNotificationRuleListSelectionPlan plan =
        IrcEventNotificationRuleListSelectionPlanner.afterDefaultReset(0);

    assertFalse(plan.selectRow());
    assertEquals(-1, plan.row());
  }
}
