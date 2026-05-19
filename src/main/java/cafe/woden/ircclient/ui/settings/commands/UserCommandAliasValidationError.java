package cafe.woden.ircclient.ui.settings.commands;

import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
import java.util.Objects;

public record UserCommandAliasValidationError(int rowIndex, String command, String message) {

  public String formatForDialog() {
    String cmd = PreferencesUiSupport.trimmedString(command);
    if (cmd.isEmpty()) cmd = "(blank)";
    String msg = PreferencesUiSupport.trimmedString(Objects.toString(message, "Invalid alias"));
    return "Row " + (rowIndex + 1) + " (/" + cmd + "):\n" + msg;
  }
}
