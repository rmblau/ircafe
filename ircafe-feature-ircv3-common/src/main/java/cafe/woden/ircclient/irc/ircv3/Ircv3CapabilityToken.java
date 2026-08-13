package cafe.woden.ircclient.irc.ircv3;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/** Canonical name/value view over one IRC CAP v3 token. */
public record Ircv3CapabilityToken(String raw, String name, String value, boolean disabled) {

  public Ircv3CapabilityToken {
    raw = Objects.toString(raw, "");
    name = Objects.toString(name, "").trim();
    value = Objects.toString(value, "").trim();
  }

  public static Optional<Ircv3CapabilityToken> parse(String rawToken) {
    String raw = Objects.toString(rawToken, "");
    String token = raw.trim();
    if (token.isEmpty()) return Optional.empty();
    if (token.startsWith(":")) token = token.substring(1).trim();

    boolean disabled = token.startsWith("-");
    while (!token.isEmpty()) {
      char leading = token.charAt(0);
      if (leading == '-' || leading == '~' || leading == '=') {
        token = token.substring(1).trim();
        continue;
      }
      break;
    }
    if (token.isEmpty()) return Optional.empty();

    String name = token;
    String value = "";
    int equals = token.indexOf('=');
    if (equals >= 0) {
      name = token.substring(0, equals).trim();
      if (equals + 1 < token.length()) {
        value = token.substring(equals + 1).trim();
      }
    }
    if (name.isEmpty()) return Optional.empty();
    return Optional.of(new Ircv3CapabilityToken(raw, name, value, disabled));
  }

  public String normalizedName() {
    return name.toLowerCase(Locale.ROOT);
  }
}
