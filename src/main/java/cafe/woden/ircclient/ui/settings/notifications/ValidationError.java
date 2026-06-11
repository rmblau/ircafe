package cafe.woden.ircclient.ui.settings.notifications;

import cafe.woden.ircclient.ui.localization.UiMessages;

public record ValidationError(int rowIndex, String label, String pattern, String message) {
  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();

  String effectiveLabel() {
    String l = label != null ? label.trim() : "";
    if (!l.isEmpty()) return l;
    String p = pattern != null ? pattern.trim() : "";
    return p.isEmpty() ? MESSAGES.text("preferences.notifications.rules.value.unnamed") : p;
  }

  String formatForInline() {
    String msg =
        message != null
            ? message.trim()
            : MESSAGES.text("preferences.notifications.rules.validation.invalidRegex.default");
    if (msg.length() > 180) msg = msg.substring(0, 180) + "…";
    return MESSAGES.text(
        "preferences.notifications.rules.validation.inline", rowIndex + 1, effectiveLabel(), msg);
  }

  public String formatForDialog() {
    String msg =
        message != null
            ? message.trim()
            : MESSAGES.text("preferences.notifications.rules.validation.invalidRegex.default");
    return MESSAGES.text(
        "preferences.notifications.rules.validation.dialog",
        rowIndex + 1,
        effectiveLabel(),
        msg,
        pattern != null ? pattern : "");
  }
}
