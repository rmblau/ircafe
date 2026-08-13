package cafe.woden.ircclient.notifications.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.config.api.NotificationRule;
import cafe.woden.ircclient.notify.api.text.NotificationTextRule;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class NotificationTextRuleAdaptersTest {

  @Test
  void convertsNullRuleListsToEmptyFeatureRuleLists() {
    assertTrue(NotificationTextRuleAdapters.toFeatureRules(null).isEmpty());
  }

  @Test
  void skipsNullRulesWhenConvertingRuleLists() {
    List<NotificationTextRule> rules =
        NotificationTextRuleAdapters.toFeatureRules(
            Arrays.asList(
                null,
                new NotificationRule(
                    "alert", NotificationRule.Type.WORD, "ping", true, false, true, null)));

    assertEquals(1, rules.size());
    assertEquals("alert", rules.get(0).label());
  }

  @Test
  void adaptsRootRuleValuesToFeatureRuleValues() {
    NotificationTextRule rule =
        NotificationTextRuleAdapters.toFeatureRule(
            new NotificationRule(
                "  regex  ",
                NotificationRule.Type.REGEX,
                "  p.ng  ",
                true,
                true,
                false,
                "#00ff00"));

    assertEquals("regex", rule.label());
    assertEquals(NotificationTextRule.Type.REGEX, rule.type());
    assertEquals("p.ng", rule.pattern());
    assertTrue(rule.enabled());
    assertTrue(rule.caseSensitive());
    assertEquals("#00FF00", rule.highlightColor());
  }

  @Test
  void defaultsNullRuleTypeToWordAndNormalizesBlankHighlight() {
    NotificationTextRule rule =
        NotificationTextRuleAdapters.toFeatureRule("label", null, "ping", true, false, true, "   ");

    assertEquals(NotificationTextRule.Type.WORD, rule.type());
    assertNull(rule.highlightColor());
  }
}
