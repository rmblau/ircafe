package cafe.woden.ircclient.irc.ircv3;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/** Parses IRCv3 CHGHOST identity changes. */
public final class Ircv3ChghostParser {

  private Ircv3ChghostParser() {}

  public record ChangeHost(String user, String host) {
    public ChangeHost {
      user = Objects.toString(user, "").trim();
      host = Objects.toString(host, "").trim();
    }

    public Optional<String> hostmaskFor(String nick) {
      String normalizedNick = Objects.toString(nick, "").trim();
      if (normalizedNick.isEmpty() || user.isBlank() || host.isBlank()) {
        return Optional.empty();
      }
      return Optional.of(normalizedNick + "!" + user + "@" + host);
    }
  }

  public static Optional<ChangeHost> parse(String command, List<String> parameters) {
    if (!"CHGHOST".equals(normalizedCommand(command))) {
      return Optional.empty();
    }
    String user = parameter(parameters, 0);
    String host = parameter(parameters, 1);
    return user.isEmpty() || host.isEmpty()
        ? Optional.empty()
        : Optional.of(new ChangeHost(user, host));
  }

  private static String normalizedCommand(String command) {
    return Objects.toString(command, "").trim().toUpperCase(Locale.ROOT);
  }

  private static String parameter(List<String> parameters, int index) {
    if (parameters == null || index < 0 || parameters.size() <= index) return "";
    String value = Objects.toString(parameters.get(index), "").trim();
    return value.startsWith(":") ? value.substring(1).trim() : value;
  }
}
