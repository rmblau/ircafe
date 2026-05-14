package cafe.woden.ircclient.ui.settings.commands;

import java.util.Objects;

public record UserCommandAliasValidationError(int rowIndex, String command, String message) {

  public String formatForDialog() {
    String cmd = Objects.toString(command, "").trim();
    if (cmd.isEmpty()) cmd = "(blank)";
    String msg = Objects.toString(message, "Invalid alias").trim();
    return "Row " + (rowIndex + 1) + " (/" + cmd + "):\n" + msg;
  }
}
