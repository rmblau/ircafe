package cafe.woden.ircclient.notify.api.panel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class NotificationPanelPopupSelectionPlannerTest {

  @Test
  void skipsWhenEventIsNotPopupTrigger() {
    NotificationPanelPopupSelectionPlan plan =
        NotificationPanelPopupSelectionPlanner.plan(false, 2, false);

    assertFalse(plan.showMenu());
    assertFalse(plan.selectRow());
    assertFalse(plan.clearSelection());
  }

  @Test
  void selectsUnselectedPopupRow() {
    NotificationPanelPopupSelectionPlan plan =
        NotificationPanelPopupSelectionPlanner.plan(true, 4, false);

    assertTrue(plan.showMenu());
    assertTrue(plan.selectRow());
    assertEquals(4, plan.rowToSelect());
    assertFalse(plan.clearSelection());
  }

  @Test
  void keepsExistingSelectionForSelectedPopupRow() {
    NotificationPanelPopupSelectionPlan plan =
        NotificationPanelPopupSelectionPlanner.plan(true, 4, true);

    assertTrue(plan.showMenu());
    assertFalse(plan.selectRow());
    assertFalse(plan.clearSelection());
  }

  @Test
  void clearsSelectionWhenPopupIsOutsideRows() {
    NotificationPanelPopupSelectionPlan plan =
        NotificationPanelPopupSelectionPlanner.plan(true, -1, false);

    assertTrue(plan.showMenu());
    assertFalse(plan.selectRow());
    assertTrue(plan.clearSelection());
  }
}
