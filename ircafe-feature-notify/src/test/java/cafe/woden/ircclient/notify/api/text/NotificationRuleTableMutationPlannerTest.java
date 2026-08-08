package cafe.woden.ircclient.notify.api.text;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class NotificationRuleTableMutationPlannerTest {

  @Test
  void selectedRowSkipsWhenSelectionIsMissingOrOutOfRange() {
    assertFalse(NotificationRuleTableMutationPlanner.selectedRow(-1, 2).proceed());
    assertFalse(NotificationRuleTableMutationPlanner.selectedRow(2, 2).proceed());
    assertFalse(NotificationRuleTableMutationPlanner.selectedRow(0, -1).proceed());
  }

  @Test
  void selectedRowPlansCurrentRowForOperations() {
    NotificationRuleTableMutationPlan plan =
        NotificationRuleTableMutationPlanner.selectedRow(1, 3);

    assertTrue(plan.proceed());
    assertEquals(1, plan.row());
    assertEquals(1, plan.targetRow());
    assertTrue(plan.selectRow());
    assertEquals(1, plan.rowToSelect());
  }

  @Test
  void afterMutationSelectsValidReturnedRowOnly() {
    NotificationRuleTableMutationPlan plan =
        NotificationRuleTableMutationPlanner.afterMutation(2, 3);

    assertTrue(plan.proceed());
    assertEquals(2, plan.rowToSelect());
    assertFalse(NotificationRuleTableMutationPlanner.afterMutation(3, 3).proceed());
  }

  @Test
  void moveSkipsMissingSelectionAndOutOfRangeTargets() {
    assertFalse(NotificationRuleTableMutationPlanner.move(-1, 3, 1).proceed());
    assertFalse(NotificationRuleTableMutationPlanner.move(0, 3, -1).proceed());
    assertFalse(NotificationRuleTableMutationPlanner.move(2, 3, 1).proceed());
  }

  @Test
  void movePlansTargetRowAndSelection() {
    NotificationRuleTableMutationPlan plan = NotificationRuleTableMutationPlanner.move(2, 4, -1);

    assertTrue(plan.proceed());
    assertEquals(2, plan.row());
    assertEquals(1, plan.targetRow());
    assertEquals(1, plan.rowToSelect());
  }

  @Test
  void afterRemovalSelectsNextRowWhenAvailable() {
    NotificationRuleTableMutationPlan plan =
        NotificationRuleTableMutationPlanner.afterRemoval(1, 2);

    assertTrue(plan.proceed());
    assertEquals(1, plan.row());
    assertEquals(1, plan.rowToSelect());
    assertFalse(plan.clearSelection());
  }

  @Test
  void afterRemovalSelectsPreviousRowWhenLastRowWasRemoved() {
    NotificationRuleTableMutationPlan plan =
        NotificationRuleTableMutationPlanner.afterRemoval(2, 2);

    assertTrue(plan.proceed());
    assertEquals(1, plan.rowToSelect());
  }

  @Test
  void afterRemovalClearsSelectionWhenNoRowsRemain() {
    NotificationRuleTableMutationPlan plan =
        NotificationRuleTableMutationPlanner.afterRemoval(0, 0);

    assertTrue(plan.proceed());
    assertFalse(plan.selectRow());
    assertTrue(plan.clearSelection());
  }
}
