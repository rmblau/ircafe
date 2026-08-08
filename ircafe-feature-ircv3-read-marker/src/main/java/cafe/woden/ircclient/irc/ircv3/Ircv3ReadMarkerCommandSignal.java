package cafe.woden.ircclient.irc.ircv3;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/** Transport-independent MARKREAD command observation. */
public record Ircv3ReadMarkerCommandSignal(String target, String marker) {

  public Ircv3ReadMarkerCommandSignal {
    target = normalize(target);
    marker = normalize(marker);
  }

  public static boolean handles(String command) {
    return "MARKREAD".equals(normalize(command).toUpperCase(Locale.ROOT));
  }

  public static Optional<Ircv3ReadMarkerCommandSignal> parse(
      String command, List<String> parsedLine) {
    if (!handles(command)) return Optional.empty();
    return Optional.of(
        new Ircv3ReadMarkerCommandSignal(firstParam(parsedLine), secondParam(parsedLine)));
  }

  private static String firstParam(List<String> parsedLine) {
    if (parsedLine == null || parsedLine.isEmpty()) return "";
    return stripLeadingColon(parsedLine.get(0));
  }

  private static String secondParam(List<String> parsedLine) {
    if (parsedLine == null || parsedLine.size() < 2) return "";
    return stripLeadingColon(parsedLine.get(1));
  }

  private static String stripLeadingColon(String raw) {
    String value = normalize(raw);
    if (value.startsWith(":")) value = value.substring(1).trim();
    return value;
  }

  private static String normalize(String value) {
    return Objects.toString(value, "").trim();
  }
}
