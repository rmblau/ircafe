package cafe.woden.ircclient.notify.api.panel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class NotificationPanelRowAccessPlannerTest {

  @Test
  void rowAtViewAcceptsOnlyRowsInsideCurrentBounds() {
    assertFalse(NotificationPanelRowAccessPlanner.rowAtView(-1, 3).valid());
    assertTrue(NotificationPanelRowAccessPlanner.rowAtView(0, 3).valid());
    assertTrue(NotificationPanelRowAccessPlanner.rowAtView(2, 3).valid());
    assertFalse(NotificationPanelRowAccessPlanner.rowAtView(3, 3).valid());
    assertFalse(NotificationPanelRowAccessPlanner.rowAtView(0, 0).valid());
  }

  @Test
  void selectedSingleRowRequiresExactlyOneSelectedRow() {
    assertFalse(NotificationPanelRowAccessPlanner.selectedSingleRow(0, 0, 3).valid());
    assertFalse(NotificationPanelRowAccessPlanner.selectedSingleRow(2, 0, 3).valid());

    NotificationPanelRowAccessPlan plan =
        NotificationPanelRowAccessPlanner.selectedSingleRow(1, 2, 3);

    assertTrue(plan.valid());
    assertEquals(2, plan.viewRow());
  }
}
