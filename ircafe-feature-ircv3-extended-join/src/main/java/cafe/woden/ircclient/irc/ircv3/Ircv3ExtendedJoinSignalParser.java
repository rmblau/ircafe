package cafe.woden.ircclient.irc.ircv3;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/** Parses IRCv3 extended-join account and real-name observations. */
public final class Ircv3ExtendedJoinSignalParser {

  private Ircv3ExtendedJoinSignalParser() {}

  public enum AccountState {
    LOGGED_IN,
    LOGGED_OUT
  }

  public record Observation(
      String nick,
      String channel,
      AccountState accountState,
      String accountName,
      String realName) {
    public Observation {
      nick = Objects.toString(nick, "").trim();
      channel = Objects.toString(channel, "").trim();
      accountState = Objects.requireNonNull(accountState, "accountState");
      accountName =
          accountState == AccountState.LOGGED_OUT ? null : normalizeNullable(accountName);
      realName = normalizeNullable(realName);
    }
  }

  public static Optional<Observation> parse(
      String sourceNick, String command, List<String> parameters) {
    if (!"JOIN".equals(normalizedCommand(command))
        || parameters == null
        || parameters.size() < 2) {
      return Optional.empty();
    }

    String nick = Objects.toString(sourceNick, "").trim();
    String channel = stripLeadingColon(parameters.get(0));
    if (nick.isEmpty() || channel.isEmpty()) return Optional.empty();

    String account = stripLeadingColon(parameters.get(1));
    AccountState state = isLoggedOut(account) ? AccountState.LOGGED_OUT : AccountState.LOGGED_IN;
    String realName = parameters.size() >= 3 ? stripLeadingColon(parameters.get(2)) : null;
    return Optional.of(new Observation(nick, channel, state, account, realName));
  }

  private static String normalizedCommand(String command) {
    return Objects.toString(command, "").trim().toUpperCase(Locale.ROOT);
  }

  private static boolean isLoggedOut(String account) {
    String value = Objects.toString(account, "").trim();
    return value.isEmpty() || "*".equals(value) || "0".equals(value);
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
