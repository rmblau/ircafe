package cafe.woden.ircclient.notify.api.text;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class NotificationTextRuleEditSubmissionPlannerTest {

  @Test
  void trimsLabelAndPatternAndPreservesWordWholeWord() {
    NotificationTextRuleEditSubmissionPlan plan =
        NotificationTextRuleEditSubmissionPlanner.plan(
            "  greeting  ", NotificationTextRule.Type.WORD, "  hello  ", true, false, true, "#abc");

    assertEquals("greeting", plan.label());
    assertEquals(NotificationTextRule.Type.WORD, plan.type());
    assertEquals("hello", plan.pattern());
    assertTrue(plan.enabled());
    assertTrue(plan.wholeWord());
    assertEquals("#abc", plan.highlightFg());
  }

  @Test
  void clearsWholeWordForRegexRules() {
    NotificationTextRuleEditSubmissionPlan plan =
        NotificationTextRuleEditSubmissionPlanner.plan(
            "", NotificationTextRule.Type.REGEX, "  h.*o  ", true, true, true, null);

    assertEquals(NotificationTextRule.Type.REGEX, plan.type());
    assertEquals("h.*o", plan.pattern());
    assertTrue(plan.caseSensitive());
    assertFalse(plan.wholeWord());
  }
}
