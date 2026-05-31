package cafe.woden.ircclient.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import cafe.woden.ircclient.config.api.NotificationRule;
import org.junit.jupiter.api.Test;

class NotificationRuleColorNormalizationTest {

  @Test
  void normalizesNotificationRuleHighlightColor() {
    assertEquals(
        "#FF9900",
        new NotificationRule("Ping", NotificationRule.Type.WORD, "ping", true, false, true, "#f90")
            .highlightFg());
    assertEquals(
        "#AABBCC",
        new NotificationRule(
                "Ping", NotificationRule.Type.WORD, "ping", true, false, true, "aabbcc")
            .highlightFg());
  }

  @Test
  void normalizesNotificationRulePropertiesHighlightColor() {
    assertEquals(
        "#FF9900",
        new NotificationRuleProperties(
                true, "Ping", NotificationRuleProperties.Type.WORD, "ping", false, true, "#f90")
            .highlightFg());
    assertEquals(
        "#AABBCC",
        new NotificationRuleProperties(
                true, "Ping", NotificationRuleProperties.Type.WORD, "ping", false, true, "aabbcc")
            .highlightFg());
  }

  @Test
  void rejectsBlankOrInvalidHighlightColors() {
    assertNull(
        new NotificationRule(
                "Ping", NotificationRule.Type.WORD, "ping", true, false, true, "not-a-color")
            .highlightFg());
    assertNull(
        new NotificationRuleProperties(
                true, "Ping", NotificationRuleProperties.Type.WORD, "ping", false, true, " ")
            .highlightFg());
  }
}
