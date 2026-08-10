package cafe.woden.ircclient.irc.ircv3;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/** Parses structured IRCv3 account-notify observations. */
public final class Ircv3AccountNotifySignalParser {

  private Ircv3AccountNotifySignalParser() {}

  public enum AccountState {
    LOGGED_IN,
    LOGGED_OUT
  }

  public record Observation(String nick, String hostmask, AccountState state, String accountName) {
    public Observation {
      nick = Objects.toString(nick, "").trim();
      hostmask = Objects.toString(hostmask, "").trim();
      state = Objects.requireNonNull(state, "state");
      accountName = state == AccountState.LOGGED_OUT ? null : normalizeNullable(accountName);
    }
  }

  public static Optional<Observation> parse(
      String sourceNick, String command, String rawLine, List<String> parameters) {
    if (!"ACCOUNT".equals(normalizedCommand(command))) {
      return Optional.empty();
    }
    String nick = Objects.toString(sourceNick, "").trim();
    if (nick.isEmpty()) return Optional.empty();

    String account =
        parameters == null || parameters.isEmpty() ? "" : stripLeadingColon(parameters.getFirst());
    AccountState state = isLoggedOut(account) ? AccountState.LOGGED_OUT : AccountState.LOGGED_IN;
    return Optional.of(new Observation(nick, observedHostmask(rawLine), state, account));
  }

  private static String normalizedCommand(String command) {
    return Objects.toString(command, "").trim().toUpperCase(Locale.ROOT);
  }

  private static boolean isLoggedOut(String account) {
    String value = Objects.toString(account, "").trim();
    return value.isEmpty() || "*".equals(value) || "0".equals(value);
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
