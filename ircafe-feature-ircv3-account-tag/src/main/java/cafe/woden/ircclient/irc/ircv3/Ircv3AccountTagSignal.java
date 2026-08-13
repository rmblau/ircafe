package cafe.woden.ircclient.irc.ircv3;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Stateless account-tag observation parsed from a tagged IRC command. */
public record Ircv3AccountTagSignal(String nick, String rawAccount) {

  public Ircv3AccountTagSignal {
    nick = Objects.toString(nick, "").trim();
    rawAccount = Objects.toString(rawAccount, "").trim();
  }

  public static Optional<Ircv3AccountTagSignal> fromTags(String nick, Map<String, String> tags) {
    String normalizedNick = Objects.toString(nick, "").trim();
    if (normalizedNick.isEmpty() || tags == null || !tags.containsKey("account")) {
      return Optional.empty();
    }
    return Optional.of(new Ircv3AccountTagSignal(normalizedNick, tags.get("account")));
  }
}
