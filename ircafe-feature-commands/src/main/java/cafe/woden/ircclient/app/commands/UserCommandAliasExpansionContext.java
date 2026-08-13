package cafe.woden.ircclient.app.commands;

import java.util.Objects;

/** Feature-safe context values available while expanding a user command alias. */
public record UserCommandAliasExpansionContext(
    String serverId,
    String target,
    String channel,
    String nick,
    String hexChatTime,
    String hexChatVersion,
    String hexChatMachine) {

  public UserCommandAliasExpansionContext {
    serverId = normalize(serverId);
    target = normalize(target);
    channel = normalize(channel);
    nick = normalize(nick);
    hexChatTime = normalize(hexChatTime);
    hexChatVersion = normalize(hexChatVersion);
    hexChatMachine = normalize(hexChatMachine);
  }

  public static UserCommandAliasExpansionContext empty() {
    return new UserCommandAliasExpansionContext("", "", "", "", "", "", "");
  }

  private static String normalize(String value) {
    return Objects.toString(value, "").trim();
  }
}
