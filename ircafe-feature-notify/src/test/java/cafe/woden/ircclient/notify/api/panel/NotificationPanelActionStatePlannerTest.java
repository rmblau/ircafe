package cafe.woden.ircclient.notify.api.panel;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class NotificationPanelActionStatePlannerTest {

  @Test
  void disablesEveryActionWhenThereAreNoRowsOrSelections() {
    NotificationPanelActionStatePlan plan =
        NotificationPanelActionStatePlanner.plan(0, 0, "msg-1", true);

    assertFalse(plan.jumpToMessageEnabled());
    assertFalse(plan.clearSelectedEnabled());
    assertFalse(plan.clearAllEnabled());
    assertFalse(plan.exportSelectedEnabled());
    assertFalse(plan.exportAllEnabled());
  }

  @Test
  void enablesBulkActionsWhenRowsAndSelectionExist() {
    NotificationPanelActionStatePlan plan =
        NotificationPanelActionStatePlanner.plan(3, 2, "msg-1", true);

    assertFalse(plan.jumpToMessageEnabled());
    assertTrue(plan.clearSelectedEnabled());
    assertTrue(plan.clearAllEnabled());
    assertTrue(plan.exportSelectedEnabled());
    assertTrue(plan.exportAllEnabled());
  }

  @Test
  void jumpRequiresSingleSelectionMessageIdAndTarget() {
    assertTrue(
        NotificationPanelActionStatePlanner.plan(3, 1, " msg-1 ", true).jumpToMessageEnabled());
    assertFalse(NotificationPanelActionStatePlanner.plan(3, 1, " ", true).jumpToMessageEnabled());
    assertFalse(
        NotificationPanelActionStatePlanner.plan(3, 1, "msg-1", false).jumpToMessageEnabled());
    assertFalse(
        NotificationPanelActionStatePlanner.plan(3, 2, "msg-1", true).jumpToMessageEnabled());
  }
}
