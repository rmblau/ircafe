package cafe.woden.ircclient.irc.ircv3;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/** Parses transport-neutral IRC INVITE observations used by invite-notify. */
public final class Ircv3InviteNotifyParser {

  private Ircv3InviteNotifyParser() {}

  public record Observation(String fromNick, String inviteeNick, String channel, String reason) {
    public Observation {
      fromNick = Objects.toString(fromNick, "").trim();
      inviteeNick = Objects.toString(inviteeNick, "").trim();
      channel = Objects.toString(channel, "").trim();
      reason = Objects.toString(reason, "").trim();
    }
  }

  public static Optional<Observation> parse(
      String sourceNick, String command, String rawLine, List<String> parameters) {
    if (!"INVITE".equals(normalizedCommand(command))) {
      return Optional.empty();
    }

    String from = Objects.toString(sourceNick, "").trim();
    String invitee = parameter(parameters, 0);
    String channel = parameter(parameters, 1);
    String trailing = trailing(rawLine);
    String reason = "";

    if (channel.isBlank()) {
      int firstSpace = trailing.indexOf(' ');
      if (firstSpace > 0) {
        channel = trailing.substring(0, firstSpace).trim();
        reason = trailing.substring(firstSpace + 1).trim();
      } else {
        channel = trailing;
      }
    } else if (!trailing.isBlank() && !trailing.equalsIgnoreCase(channel)) {
      reason =
          trailing.startsWith(channel + " ")
              ? trailing.substring(channel.length()).trim()
              : trailing;
    }

    channel = stripLeadingColon(channel);
    reason = stripLeadingColon(reason);
    if (channel.isBlank()) return Optional.empty();
    return Optional.of(new Observation(from, invitee, channel, reason));
  }

  private static String normalizedCommand(String command) {
    return Objects.toString(command, "").trim().toUpperCase(Locale.ROOT);
  }

  private static String parameter(List<String> parameters, int index) {
    if (parameters == null || index < 0 || parameters.size() <= index) return "";
    return stripLeadingColon(parameters.get(index));
  }

  private static String trailing(String rawLine) {
    String line = Objects.toString(rawLine, "").trim();
    int index = line.indexOf(" :");
    return index < 0 ? "" : line.substring(index + 2).trim();
  }

  private static String stripLeadingColon(String raw) {
    String value = Objects.toString(raw, "").trim();
    return value.startsWith(":") ? value.substring(1).trim() : value;
  }
}
