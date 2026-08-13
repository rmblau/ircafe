package cafe.woden.ircclient.notify.api.text;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class NotificationRuleTableSelectionPlannerTest {

  @Test
  void disablesAllActionsWhenNoRowIsSelected() {
    NotificationRuleTableSelectionPlan plan = NotificationRuleTableSelectionPlanner.plan(-1, 2);

    assertFalse(plan.editEnabled());
    assertFalse(plan.duplicateEnabled());
    assertFalse(plan.removeEnabled());
    assertFalse(plan.moveUpEnabled());
    assertFalse(plan.moveDownEnabled());
  }

  @Test
  void selectedFirstRowEnablesCoreActionsAndMoveDownOnly() {
    NotificationRuleTableSelectionPlan plan = NotificationRuleTableSelectionPlanner.plan(0, 2);

    assertTrue(plan.editEnabled());
    assertTrue(plan.duplicateEnabled());
    assertTrue(plan.removeEnabled());
    assertFalse(plan.moveUpEnabled());
    assertTrue(plan.moveDownEnabled());
  }

  @Test
  void selectedLastRowEnablesCoreActionsAndMoveUpOnly() {
    NotificationRuleTableSelectionPlan plan = NotificationRuleTableSelectionPlanner.plan(1, 2);

    assertTrue(plan.editEnabled());
    assertTrue(plan.duplicateEnabled());
    assertTrue(plan.removeEnabled());
    assertTrue(plan.moveUpEnabled());
    assertFalse(plan.moveDownEnabled());
  }

  @Test
  void disablesMovementForOnlyRow() {
    NotificationRuleTableSelectionPlan plan = NotificationRuleTableSelectionPlanner.plan(0, 1);

    assertTrue(plan.editEnabled());
    assertTrue(plan.duplicateEnabled());
    assertTrue(plan.removeEnabled());
    assertFalse(plan.moveUpEnabled());
    assertFalse(plan.moveDownEnabled());
  }

  @Test
  void outOfRangeSelectionAndNegativeCountsDisableAllActions() {
    NotificationRuleTableSelectionPlan outOfRange =
        NotificationRuleTableSelectionPlanner.plan(3, 2);
    NotificationRuleTableSelectionPlan negativeCount =
        NotificationRuleTableSelectionPlanner.plan(0, -1);

    assertFalse(outOfRange.editEnabled());
    assertFalse(outOfRange.moveDownEnabled());
    assertFalse(negativeCount.editEnabled());
    assertFalse(negativeCount.moveDownEnabled());
  }
}
