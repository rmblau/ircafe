package cafe.woden.ircclient.notify.api.text;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class NotificationTextRuleEditSeedPlannerTest {
  @Test
  void defaultSeedStartsAsEnabledWordRuleForAddingRules() {
    NotificationTextRuleEditSeedPlan plan = NotificationTextRuleEditSeedPlanner.defaultSeed();

    assertEquals("", plan.label());
    assertEquals(NotificationTextRule.Type.WORD, plan.type());
    assertEquals("", plan.pattern());
    assertTrue(plan.enabled());
    assertFalse(plan.caseSensitive());
    assertTrue(plan.wholeWord());
    assertNull(plan.highlightFg());
  }

  @Test
  void emptyRowSeedPreservesPriorNullTableRowDefaults() {
    NotificationTextRuleEditSeedPlan plan = NotificationTextRuleEditSeedPlanner.emptyRowSeed();

    assertEquals("", plan.label());
    assertEquals(NotificationTextRule.Type.WORD, plan.type());
    assertEquals("", plan.pattern());
    assertFalse(plan.enabled());
    assertFalse(plan.caseSensitive());
    assertTrue(plan.wholeWord());
    assertNull(plan.highlightFg());
  }

  @Test
  void trimsTextSeedValuesWithoutChangingEnabledState() {
    NotificationTextRuleEditSeedPlan plan =
        NotificationTextRuleEditSeedPlanner.plan(
            "  Ops  ", NotificationTextRule.Type.WORD, "  hello  ", true, true, false, " #abc123 ");

    assertEquals("Ops", plan.label());
    assertEquals("hello", plan.pattern());
    assertTrue(plan.enabled());
    assertTrue(plan.caseSensitive());
    assertFalse(plan.wholeWord());
    assertEquals("#abc123", plan.highlightFg());
  }

  @Test
  void blankPatternDoesNotForceDisableForEditSeeds() {
    NotificationTextRuleEditSeedPlan plan =
        NotificationTextRuleEditSeedPlanner.plan(
            "empty", NotificationTextRule.Type.WORD, "   ", true, false, true, null);

    assertTrue(plan.enabled());
    assertEquals("", plan.pattern());
  }

  @Test
  void regexSeedClearsWholeWord() {
    NotificationTextRuleEditSeedPlan plan =
        NotificationTextRuleEditSeedPlanner.plan(
            "regex", NotificationTextRule.Type.REGEX, ".*", true, false, true, null);

    assertEquals(NotificationTextRule.Type.REGEX, plan.type());
    assertFalse(plan.wholeWord());
  }

  @Test
  void nullTypeFallsBackToWordAndBlankHighlightBecomesNull() {
    NotificationTextRuleEditSeedPlan plan =
        NotificationTextRuleEditSeedPlanner.plan("x", null, "pattern", true, false, true, "  ");

    assertEquals(NotificationTextRule.Type.WORD, plan.type());
    assertNull(plan.highlightFg());
  }
}
