package cafe.woden.ircclient.notify.api.irc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class IrcEventNotificationPresetApplyPlannerTest {

  @Test
  void skipsNullOrEmptyPreset() {
    assertFalse(IrcEventNotificationPresetApplyPlanner.plan(List.of("INVITE_RECEIVED"), null).apply());
    assertFalse(IrcEventNotificationPresetApplyPlanner.plan(List.of("INVITE_RECEIVED"), List.of()).apply());
  }

  @Test
  void replacesExistingRowsByFirstMatchingEventType() {
    IrcEventNotificationPresetApplyPlan plan =
        IrcEventNotificationPresetApplyPlanner.plan(
            List.of("INVITE_RECEIVED", "KICKED", "INVITE_RECEIVED"), List.of("KICKED"));

    assertTrue(plan.apply());
    assertTrue(plan.selectRow());
    assertEquals(1, plan.firstRowToSelect());
    assertEquals(1, plan.operations().size());
    assertEquals(0, plan.operations().getFirst().presetIndex());
    assertEquals(1, plan.operations().getFirst().existingRow());
    assertTrue(plan.operations().getFirst().replaceExistingRow());
  }

  @Test
  void appendsMissingPresetRulesAfterExistingRows() {
    IrcEventNotificationPresetApplyPlan plan =
        IrcEventNotificationPresetApplyPlanner.plan(
            List.of("INVITE_RECEIVED"), List.of("KICKED", "BANNED"));

    assertTrue(plan.apply());
    assertEquals(1, plan.firstRowToSelect());
    assertEquals(2, plan.operations().size());
    assertTrue(plan.operations().get(0).appendRow());
    assertEquals(-1, plan.operations().get(0).existingRow());
    assertTrue(plan.operations().get(1).appendRow());
    assertEquals(-1, plan.operations().get(1).existingRow());
  }


  @Test
  void laterDuplicatePresetEventReplacesFirstAppendedRow() {
    IrcEventNotificationPresetApplyPlan plan =
        IrcEventNotificationPresetApplyPlanner.plan(List.of(), List.of("KICKED", "KICKED"));

    assertTrue(plan.apply());
    assertEquals(0, plan.firstRowToSelect());
    assertEquals(2, plan.operations().size());
    assertTrue(plan.operations().get(0).appendRow());
    assertTrue(plan.operations().get(1).replaceExistingRow());
    assertEquals(0, plan.operations().get(1).existingRow());
  }

  @Test
  void normalizesEventNamesCaseAndWhitespace() {
    IrcEventNotificationPresetApplyPlan plan =
        IrcEventNotificationPresetApplyPlanner.plan(
            List.of(" invite_received "), List.of("INVITE_RECEIVED"));

    assertTrue(plan.apply());
    assertEquals(0, plan.firstRowToSelect());
    assertTrue(plan.operations().getFirst().replaceExistingRow());
  }

  @Test
  void ignoresBlankPresetEvents() {
    IrcEventNotificationPresetApplyPlan plan =
        IrcEventNotificationPresetApplyPlanner.plan(List.of("INVITE_RECEIVED"), List.of(" ", "KICKED"));

    assertTrue(plan.apply());
    assertEquals(1, plan.firstRowToSelect());
    assertEquals(1, plan.operations().size());
    assertEquals(1, plan.operations().getFirst().presetIndex());
  }
}
