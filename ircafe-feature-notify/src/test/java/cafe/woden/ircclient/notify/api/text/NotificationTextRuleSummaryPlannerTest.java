package cafe.woden.ircclient.notify.api.text;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class NotificationTextRuleSummaryPlannerTest {

  @Test
  void plansTrimmedWordRuleSummaryValues() {
    NotificationTextRule rule =
        new NotificationTextRule(
            "  greeting  ", NotificationTextRule.Type.WORD, "  hello  ", true, false, true, null);

    NotificationTextRuleSummaryPlan plan = NotificationTextRuleSummaryPlanner.plan(rule);

    assertEquals("greeting", plan.label());
    assertEquals(NotificationTextRule.Type.WORD, plan.type());
    assertEquals("hello", plan.pattern());
    assertTrue(plan.wordRule());
    assertTrue(plan.wholeWord());
    assertEquals("greeting", plan.effectiveLabel());
  }

  @Test
  void fallsBackEffectiveLabelToPattern() {
    NotificationTextRule rule =
        new NotificationTextRule(
            "  ", NotificationTextRule.Type.WORD, "  hello  ", true, false, true, null);

    NotificationTextRuleSummaryPlan plan = NotificationTextRuleSummaryPlanner.plan(rule);

    assertEquals("hello", plan.effectiveLabel());
  }

  @Test
  void regexRulesDoNotExposeWholeWordOption() {
    NotificationTextRule rule =
        new NotificationTextRule(
            "rx", NotificationTextRule.Type.REGEX, "h.*o", true, true, true, null);

    NotificationTextRuleSummaryPlan plan = NotificationTextRuleSummaryPlanner.plan(rule);

    assertEquals(NotificationTextRule.Type.REGEX, plan.type());
    assertTrue(plan.caseSensitive());
    assertFalse(plan.wordRule());
    assertFalse(plan.wholeWord());
  }

  @Test
  void nullRuleUsesEmptyWordDefaults() {
    NotificationTextRuleSummaryPlan plan = NotificationTextRuleSummaryPlanner.plan(null);

    assertEquals("", plan.label());
    assertEquals(NotificationTextRule.Type.WORD, plan.type());
    assertEquals("", plan.pattern());
    assertFalse(plan.patternPresent());
    assertTrue(plan.wordRule());
    assertTrue(plan.wholeWord());
  }
}
