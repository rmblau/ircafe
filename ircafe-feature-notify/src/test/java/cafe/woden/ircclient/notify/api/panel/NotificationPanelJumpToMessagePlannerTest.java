package cafe.woden.ircclient.notify.api.panel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class NotificationPanelJumpToMessagePlannerTest {

  @Test
  void trimsMessageIdWhenTargetIsValid() {
    NotificationPanelJumpToMessagePlan plan =
        NotificationPanelJumpToMessagePlanner.plan(true, "  abc-123  ");

    assertTrue(plan.jump());
    assertEquals("abc-123", plan.messageId());
  }

  @Test
  void skipsWhenTargetIsInvalidOrMessageIdBlank() {
    assertFalse(NotificationPanelJumpToMessagePlanner.plan(false, "abc").jump());
    assertFalse(NotificationPanelJumpToMessagePlanner.plan(true, " ").jump());
    assertFalse(NotificationPanelJumpToMessagePlanner.plan(true, null).jump());
  }
}
