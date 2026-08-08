package cafe.woden.ircclient.irc.ircv3;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/** Parses structured IRCv3 away-notify observations. */
public final class Ircv3AwayNotifySignalParser {

  private Ircv3AwayNotifySignalParser() {}

  public record Observation(String nick, String hostmask, boolean away, String message) {
    public Observation {
      nick = Objects.toString(nick, "").trim();
      hostmask = Objects.toString(hostmask, "").trim();
      message = normalizeNullable(message);
    }
  }

  public static Optional<Observation> parse(
      String sourceNick, String command, String rawLine, List<String> parameters) {
    if (!"AWAY".equals(normalizedCommand(command))) {
      return Optional.empty();
    }
    String nick = Objects.toString(sourceNick, "").trim();
    if (nick.isEmpty()) return Optional.empty();

    boolean away = parameters != null && !parameters.isEmpty();
    String message = away ? stripLeadingColon(parameters.getFirst()) : null;
    return Optional.of(new Observation(nick, observedHostmask(rawLine), away, message));
  }

  private static String normalizedCommand(String command) {
    return Objects.toString(command, "").trim().toUpperCase(Locale.ROOT);
  }

  private static String observedHostmask(String rawLine) {
    String line = Objects.toString(rawLine, "").trim();
    if (line.startsWith("@")) {
      int firstSpace = line.indexOf(' ');
      if (firstSpace <= 0 || firstSpace >= line.length() - 1) return "";
      line = line.substring(firstSpace + 1);
    }
    if (!line.startsWith(":")) return "";
    int firstSpace = line.indexOf(' ');
    return firstSpace <= 1 ? "" : line.substring(1, firstSpace).trim();
  }

  private static String stripLeadingColon(String raw) {
    String value = Objects.toString(raw, "").trim();
    return value.startsWith(":") ? value.substring(1).trim() : value;
  }

  private static String normalizeNullable(String raw) {
    String value = Objects.toString(raw, "").trim();
    return value.isEmpty() ? null : value;
  }
}
