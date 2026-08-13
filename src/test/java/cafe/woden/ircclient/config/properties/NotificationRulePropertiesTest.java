package cafe.woden.ircclient.config.properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class NotificationRulePropertiesTest {
  @Test
  void constructorDelegatesTextPersistedNormalizationPolicy() {
    NotificationRuleProperties rule =
        new NotificationRuleProperties(null, "   ", null, "  ping  ", null, null, " #f90 ");

    assertTrue(rule.enabled());
    assertEquals("ping", rule.label());
    assertEquals(NotificationRuleProperties.Type.WORD, rule.type());
    assertEquals("ping", rule.pattern());
    assertFalse(rule.caseSensitive());
    assertTrue(rule.wholeWord());
    assertEquals("#FF9900", rule.highlightFg());
  }

  @Test
  void constructorDisablesBlankPatternsAndRejectsInvalidHighlightColors() {
    NotificationRuleProperties rule =
        new NotificationRuleProperties(
            true,
            " Empty ",
            NotificationRuleProperties.Type.REGEX,
            "   ",
            true,
            true,
            "not-a-color");

    assertFalse(rule.enabled());
    assertEquals("Empty", rule.label());
    assertEquals(NotificationRuleProperties.Type.REGEX, rule.type());
    assertEquals("", rule.pattern());
    assertTrue(rule.caseSensitive());
    assertTrue(rule.wholeWord());
    assertNull(rule.highlightFg());
  }
}
