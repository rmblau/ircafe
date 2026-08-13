package cafe.woden.ircclient.ui.settings.notifications;

import cafe.woden.ircclient.config.api.NotificationRule;
import cafe.woden.ircclient.notifications.api.NotificationTextRuleAdapters;
import cafe.woden.ircclient.notify.api.text.NotificationTextMatch;
import cafe.woden.ircclient.notify.api.text.NotificationTextMatchSnippetPlanner;
import cafe.woden.ircclient.notify.api.text.NotificationTextRuleMatcher;
import cafe.woden.ircclient.notify.api.text.NotificationTextRuleTestSamplePlan;
import cafe.woden.ircclient.notify.api.text.NotificationTextRuleTestSamplePlanner;
import cafe.woden.ircclient.ui.localization.UiMessages;
import java.util.List;

final class NotificationRuleTestReportSupport {
  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();

  private NotificationRuleTestReportSupport() {}

  static String buildRuleTestReport(
      List<NotificationRule> rules, List<ValidationError> errors, String sample) {
    NotificationTextRuleTestSamplePlan samplePlan =
        NotificationTextRuleTestSamplePlanner.plan(sample);
    String msg = samplePlan.matcherSample();
    if (samplePlan.empty()) {
      return MESSAGES.text("preferences.notifications.rules.test.empty");
    }

    StringBuilder out = new StringBuilder();

    if (errors != null && !errors.isEmpty()) {
      out.append(MESSAGES.text("preferences.notifications.rules.test.invalidRegexHeader"))
          .append("\n");
      int shown = 0;
      for (ValidationError e : errors) {
        if (e == null) continue;
        out.append(
                MESSAGES.text(
                    "preferences.notifications.rules.test.invalidRegexRow",
                    e.rowIndex() + 1,
                    e.effectiveLabel()))
            .append("\n");
        shown++;
        if (shown >= 5) {
          int remain = errors.size() - shown;
          if (remain > 0) {
            out.append(MESSAGES.text("preferences.notifications.rules.test.more", remain))
                .append("\n");
          }
          break;
        }
      }
      out.append("\n");
    }

    NotificationTextRuleMatcher matcher =
        NotificationTextRuleMatcher.compile(NotificationTextRuleAdapters.toFeatureRules(rules));
    List<NotificationTextMatch> matches = matcher.matchAll(msg);

    if (!matcher.compileFailures().isEmpty() && (errors == null || errors.isEmpty())) {
      out.append(MESSAGES.text("preferences.notifications.rules.test.invalidRegexWarning"))
          .append("\n\n");
    }

    if (matches.isEmpty()) {
      out.append(MESSAGES.text("preferences.notifications.rules.test.noMatches"));
    } else {
      out.append(
              MESSAGES.text("preferences.notifications.rules.test.matchesHeader", matches.size()))
          .append("\n");
      for (NotificationTextMatch match : matches) {
        out.append("  ").append(lineFor(match, msg)).append("\n");
      }
    }

    return out.toString().trim();
  }

  private static String lineFor(NotificationTextMatch match, String message) {
    String label =
        match.ruleLabel() != null && !match.ruleLabel().trim().isEmpty()
            ? match.ruleLabel().trim()
            : MESSAGES.text("preferences.notifications.rules.test.unnamed");
    return MESSAGES.text(
        "preferences.notifications.rules.test.matchLine",
        label,
        match.ruleType(),
        NotificationTextMatchSnippetPlanner.snippetAround(message, match.start(), match.end()));
  }
}
