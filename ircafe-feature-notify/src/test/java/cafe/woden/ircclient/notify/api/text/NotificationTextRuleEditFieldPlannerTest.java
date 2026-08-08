package cafe.woden.ircclient.notify.api.text;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class NotificationTextRuleEditFieldPlannerTest {

  @Test
  void enablesWholeWordOnlyForWordRules() {
    NotificationTextRuleEditFieldPlan wordPlan =
        NotificationTextRuleEditFieldPlanner.plan(NotificationTextRule.Type.WORD, true);
    NotificationTextRuleEditFieldPlan regexPlan =
        NotificationTextRuleEditFieldPlanner.plan(NotificationTextRule.Type.REGEX, true);

    assertTrue(wordPlan.wholeWordAvailable());
    assertTrue(wordPlan.wholeWordSelected());
    assertFalse(regexPlan.wholeWordAvailable());
    assertFalse(regexPlan.wholeWordSelected());
  }

  @Test
  void treatsNullTypeAsWordRule() {
    NotificationTextRuleEditFieldPlan plan = NotificationTextRuleEditFieldPlanner.plan(null, true);

    assertTrue(plan.wholeWordAvailable());
    assertTrue(plan.wholeWordSelected());
  }
}
