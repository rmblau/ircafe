package cafe.woden.ircclient.notify.api.irc;

import java.util.List;
import java.util.Map;

/** Feature-safe script execution plan for IRC event notification rules. */
public record IrcEventNotificationScriptPlan(
    List<String> command, String workingDirectory, Map<String, String> environment) {

  public IrcEventNotificationScriptPlan {
    command = command == null ? List.of() : List.copyOf(command);
    environment = environment == null ? Map.of() : Map.copyOf(environment);
  }
}
