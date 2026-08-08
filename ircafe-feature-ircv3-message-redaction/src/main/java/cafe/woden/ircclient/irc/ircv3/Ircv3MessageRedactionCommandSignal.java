package cafe.woden.ircclient.irc.ircv3;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/** Transport-independent inbound REDACT command observation. */
public record Ircv3MessageRedactionCommandSignal(String target, String messageId) {

  public Ircv3MessageRedactionCommandSignal {
    target = normalize(target);
    messageId = normalize(messageId);
    if (messageId.isEmpty()) throw new IllegalArgumentException("messageId must not be blank");
  }

  public static boolean handles(String command) {
    return "REDACT".equals(normalize(command).toUpperCase(Locale.ROOT));
  }

  public static Optional<Ircv3MessageRedactionCommandSignal> parse(
      String command, List<String> parsedLine) {
    if (!handles(command)) return Optional.empty();
    String messageId = secondParam(parsedLine);
    if (messageId.isEmpty()) return Optional.empty();
    return Optional.of(
        new Ircv3MessageRedactionCommandSignal(firstParam(parsedLine), messageId));
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
