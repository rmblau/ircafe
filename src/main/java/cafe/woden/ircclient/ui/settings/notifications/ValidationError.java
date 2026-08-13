package cafe.woden.ircclient.ui.settings.notifications;

import cafe.woden.ircclient.notify.api.text.NotificationTextRuleValidationDisplayPlan;
import cafe.woden.ircclient.notify.api.text.NotificationTextRuleValidationDisplayPlanner;
import cafe.woden.ircclient.notify.api.text.NotificationTextRuleValidationError;
import cafe.woden.ircclient.ui.localization.UiMessages;

public record ValidationError(int rowIndex, String label, String pattern, String message) {
  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();

  static ValidationError from(NotificationTextRuleValidationError error) {
    if (error == null) return null;
    return new ValidationError(error.rowIndex(), error.label(), error.pattern(), error.message());
  }

  String effectiveLabel() {
    return displayPlan().effectiveLabel();
  }

  String formatForInline() {
    NotificationTextRuleValidationDisplayPlan plan = displayPlan();
    return MESSAGES.text(
        "preferences.notifications.rules.validation.inline",
        plan.rowNumber(),
        plan.effectiveLabel(),
        plan.inlineMessage());
  }

  public String formatForDialog() {
    NotificationTextRuleValidationDisplayPlan plan = displayPlan();
    return MESSAGES.text(
        "preferences.notifications.rules.validation.dialog",
        plan.rowNumber(),
        plan.effectiveLabel(),
        plan.dialogMessage(),
        plan.patternForDialog());
  }

  private NotificationTextRuleValidationDisplayPlan displayPlan() {
    return NotificationTextRuleValidationDisplayPlanner.plan(
        new NotificationTextRuleValidationError(rowIndex, label, pattern, message),
        MESSAGES.text("preferences.notifications.rules.value.unnamed"),
        MESSAGES.text("preferences.notifications.rules.validation.invalidRegex.default"));
  }
}
