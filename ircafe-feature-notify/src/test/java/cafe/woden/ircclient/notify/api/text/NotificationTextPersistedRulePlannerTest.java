package cafe.woden.ircclient.notify.api.text;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class NotificationTextPersistedRulePlannerTest {
  @Test
  void appliesPersistedDefaultsAndDisablesBlankPatterns() {
    NotificationTextPersistedRulePlan plan = plan(null, null, null, null, null, null, null);

    assertFalse(plan.enabled());
    assertEquals("", plan.label());
    assertEquals(NotificationTextRule.Type.WORD.name(), plan.type());
    assertEquals("", plan.pattern());
    assertFalse(plan.caseSensitive());
    assertTrue(plan.wholeWord());
    assertNull(plan.highlightFg());
  }

  @Test
  void trimsLabelAndPatternAndUsesPatternAsBlankLabelFallback() {
    NotificationTextPersistedRulePlan plan =
        plan(null, "   ", NotificationTextRule.Type.WORD.name(), "  ping  ", null, null, null);

    assertTrue(plan.enabled());
    assertEquals("ping", plan.label());
    assertEquals("ping", plan.pattern());
  }

  @Test
  void preservesExplicitDisabledStateForNonBlankPattern() {
    NotificationTextPersistedRulePlan plan =
        plan(false, "Ping", NotificationTextRule.Type.WORD.name(), "ping", true, false, null);

    assertFalse(plan.enabled());
    assertTrue(plan.caseSensitive());
    assertFalse(plan.wholeWord());
  }

  @Test
  void unknownOrBlankTypeFallsBackToWord() {
    assertEquals(
        NotificationTextRule.Type.WORD.name(),
        plan(null, null, " ", "ping", null, null, null).type());
    assertEquals(
        NotificationTextRule.Type.WORD.name(),
        plan(null, null, "not-a-type", "ping", null, null, null).type());
  }

  @Test
  void persistedRegexWholeWordValueIsPreserved() {
    NotificationTextPersistedRulePlan plan =
        plan(true, "Rx", NotificationTextRule.Type.REGEX.name(), ".*", false, true, null);

    assertEquals(NotificationTextRule.Type.REGEX.name(), plan.type());
    assertTrue(plan.wholeWord());
  }

  @Test
  void normalizesLenientHighlightColors() {
    assertEquals("#FF9900", plan(true, "x", "WORD", "x", false, true, "#f90").highlightFg());
    assertEquals("#AABBCC", plan(true, "x", "WORD", "x", false, true, "aabbcc").highlightFg());
    assertEquals("#FF9900", plan(true, "x", "WORD", "x", false, true, "0xf90").highlightFg());
  }

  @Test
  void rejectsBlankOrInvalidHighlightColors() {
    assertNull(plan(true, "x", "WORD", "x", false, true, " ").highlightFg());
    assertNull(plan(true, "x", "WORD", "x", false, true, "not-a-color").highlightFg());
  }

  private static NotificationTextPersistedRulePlan plan(
      Boolean enabled,
      String label,
      String type,
      String pattern,
      Boolean caseSensitive,
      Boolean wholeWord,
      String highlightFg) {
    return NotificationTextPersistedRulePlanner.plan(
        enabled, label, type, pattern, caseSensitive, wholeWord, highlightFg);
  }
}
