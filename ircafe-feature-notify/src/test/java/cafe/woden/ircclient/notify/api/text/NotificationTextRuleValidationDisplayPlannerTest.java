package cafe.woden.ircclient.notify.api.text;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class NotificationTextRuleValidationDisplayPlannerTest {

  @Test
  void trimsLabelAndMessageForDisplay() {
    NotificationTextRuleValidationDisplayPlan plan =
        NotificationTextRuleValidationDisplayPlanner.plan(
            new NotificationTextRuleValidationError(2, "  Friendly  ", "  [  ", "  broken  "),
            "(unnamed)",
            "Invalid regex");

    assertEquals(3, plan.rowNumber());
    assertEquals("Friendly", plan.effectiveLabel());
    assertEquals("broken", plan.inlineMessage());
    assertEquals("broken", plan.dialogMessage());
    assertEquals("[", plan.patternForDialog());
  }

  @Test
  void fallsBackToPatternThenUnnamedLabel() {
    NotificationTextRuleValidationDisplayPlan patternPlan =
        NotificationTextRuleValidationDisplayPlanner.plan(
            new NotificationTextRuleValidationError(0, "  ", "  h.*o  ", "message"),
            "(unnamed)",
            "Invalid regex");
    NotificationTextRuleValidationDisplayPlan unnamedPlan =
        NotificationTextRuleValidationDisplayPlanner.plan(
            new NotificationTextRuleValidationError(0, "  ", "  ", "message"),
            "(unnamed)",
            "Invalid regex");

    assertEquals("h.*o", patternPlan.effectiveLabel());
    assertEquals("(unnamed)", unnamedPlan.effectiveLabel());
  }

  @Test
  void usesDefaultMessageAndTruncatesInlineMessageOnly() {
    String longMessage = "x".repeat(12);

    NotificationTextRuleValidationDisplayPlan plan =
        NotificationTextRuleValidationDisplayPlanner.plan(
            new NotificationTextRuleValidationError(0, "rule", "[", "  "),
            "(unnamed)",
            longMessage,
            5);

    assertEquals("xxxxx…", plan.inlineMessage());
    assertEquals(longMessage, plan.dialogMessage());
  }
}
