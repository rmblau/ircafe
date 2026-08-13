package cafe.woden.ircclient.notify.api.panel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class NotificationPanelViewRowsPlannerTest {

  @Test
  void returnsAllViewRowsForPositiveCounts() {
    assertEquals(List.of(0, 1, 2), NotificationPanelViewRowsPlanner.allRows(3));
  }

  @Test
  void rejectsNonPositiveAllRowCounts() {
    assertEquals(List.of(), NotificationPanelViewRowsPlanner.allRows(0));
    assertEquals(List.of(), NotificationPanelViewRowsPlanner.allRows(-1));
  }

  @Test
  void sortsAndFiltersSelectedRows() {
    assertEquals(
        List.of(1, 3, 4), NotificationPanelViewRowsPlanner.selectedRows(new int[] {4, -2, 1, 3}));
  }

  @Test
  void handlesMissingSelectedRows() {
    assertEquals(List.of(), NotificationPanelViewRowsPlanner.selectedRows(null));
    assertEquals(List.of(), NotificationPanelViewRowsPlanner.selectedRows(new int[0]));
  }
}
