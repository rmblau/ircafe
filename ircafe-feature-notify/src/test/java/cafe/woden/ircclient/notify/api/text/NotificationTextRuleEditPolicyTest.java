package cafe.woden.ircclient.notify.api.text;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class NotificationTextRuleEditPolicyTest {

  @Test
  void reportsEnabledRegexValidationErrorsWithRowContext() {
    List<NotificationTextRuleValidationError> errors =
        NotificationTextRuleEditPolicy.validationErrors(
            List.of(
                new NotificationTextRule(
                    "Broken", NotificationTextRule.Type.REGEX, "[", true, false, false, null),
                new NotificationTextRule(
                    "Word", NotificationTextRule.Type.WORD, "[", true, false, true, null)));

    assertEquals(1, errors.size());
    NotificationTextRuleValidationError error = errors.getFirst();
    assertEquals(0, error.rowIndex());
    assertEquals("Broken", error.label());
    assertEquals("[", error.pattern());
  }

  @Test
  void ignoresDisabledBlankAndNonRegexRules() {
    assertEquals(
        List.of(),
        NotificationTextRuleEditPolicy.validationErrors(
            List.of(
                new NotificationTextRule(
                    "Disabled", NotificationTextRule.Type.REGEX, "[", false, false, false, null),
                new NotificationTextRule(
                    "Blank", NotificationTextRule.Type.REGEX, "", true, false, false, null),
                new NotificationTextRule(
                    "Word", NotificationTextRule.Type.WORD, "[", true, false, true, null))));
  }

  @Test
  void validRegexHasNoError() {
    assertNull(
        NotificationTextRuleEditPolicy.validateRule(
            4,
            new NotificationTextRule(
                "Valid", NotificationTextRule.Type.REGEX, "\\w+", true, false, false, null)));
  }

  @Test
  void wholeWordOptionOnlyAppliesToWordRules() {
    assertTrue(
        NotificationTextRuleEditPolicy.wholeWordOptionAvailable(NotificationTextRule.Type.WORD));
    assertFalse(
        NotificationTextRuleEditPolicy.wholeWordOptionAvailable(NotificationTextRule.Type.REGEX));
    assertTrue(NotificationTextRuleEditPolicy.wholeWordOptionAvailable(null));

    assertTrue(
        NotificationTextRuleEditPolicy.normalizeWholeWord(NotificationTextRule.Type.WORD, true));
    assertFalse(
        NotificationTextRuleEditPolicy.normalizeWholeWord(NotificationTextRule.Type.REGEX, true));

    NotificationTextRule regex =
        new NotificationTextRule(
            "regex", NotificationTextRule.Type.REGEX, "h.*o", true, false, true, null);
    assertFalse(regex.wholeWord());
  }
}
