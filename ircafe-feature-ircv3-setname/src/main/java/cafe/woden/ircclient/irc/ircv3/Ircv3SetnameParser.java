package cafe.woden.ircclient.irc.ircv3;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/** Parses IRCv3 SETNAME identity changes. */
public final class Ircv3SetnameParser {

  private Ircv3SetnameParser() {}

  public record SetName(String realName) {
    public SetName {
      realName = Objects.toString(realName, "").trim();
    }
  }

  public static Optional<SetName> parse(String command, List<String> parameters) {
    if (!"SETNAME".equals(normalizedCommand(command))) {
      return Optional.empty();
    }
    String realName = parameter(parameters, 0);
    return realName.isEmpty() ? Optional.empty() : Optional.of(new SetName(realName));
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
