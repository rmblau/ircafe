package cafe.woden.ircclient.ui.settings.notifications;

import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.config.api.NotificationRule;
import java.util.List;
import org.junit.jupiter.api.Test;

class NotificationRuleTestReportSupportTest {

  @Test
  void reportUsesFeatureMatcherForWordAndRegexMatches() {
    List<NotificationRule> rules =
        List.of(
            new NotificationRule(
                "Ping", NotificationRule.Type.WORD, "ping", true, false, true, null),
            new NotificationRule(
                "Channel", NotificationRule.Type.REGEX, "#\\w+", true, false, false, null));

    String report =
        NotificationRuleTestReportSupport.buildRuleTestReport(
            rules, List.of(), "alice says ping in #ircafe");

    assertTrue(report.contains("Matches (2):"));
    assertTrue(report.contains("- Ping [WORD]: alice says [ping] in #ircafe"));
    assertTrue(report.contains("- Channel [REGEX]: alice says ping in [#ircafe]"));
  }

  @Test
  void reportShowsCompileWarningWhenRegexErrorsWereNotPrecomputed() {
    List<NotificationRule> rules =
        List.of(
            new NotificationRule(
                "Broken", NotificationRule.Type.REGEX, "[", true, false, false, null),
            new NotificationRule(
                "Ping", NotificationRule.Type.WORD, "ping", true, false, false, null));

    String report = NotificationRuleTestReportSupport.buildRuleTestReport(rules, List.of(), "ping");

    assertTrue(report.contains("Some REGEX rules are invalid and were ignored."));
    assertTrue(report.contains("- Ping [WORD]: [ping]"));
  }

  @Test
  void reportUsesFeatureSamplePlannerLimitBeforeMatching() {
    NotificationRule rule =
        new NotificationRule("Ping", NotificationRule.Type.WORD, "ping", true, false, false, null);
    String sample = "x".repeat(800) + " ping";

    String report =
        NotificationRuleTestReportSupport.buildRuleTestReport(List.of(rule), List.of(), sample);

    assertTrue(report.contains("No matches."));
  }
}
