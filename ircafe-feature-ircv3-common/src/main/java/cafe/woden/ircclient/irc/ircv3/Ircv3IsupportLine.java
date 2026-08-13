package cafe.woden.ircclient.irc.ircv3;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/** Transport-neutral representation of one RPL_ISUPPORT (005) line. */
public record Ircv3IsupportLine(List<Token> tokens) {

  public Ircv3IsupportLine {
    tokens = tokens == null ? List.of() : List.copyOf(tokens);
  }

  /** Parse one raw IRC line, including optional IRCv3 tags and server prefix. */
  public static Optional<Ircv3IsupportLine> parse(String rawLine) {
    String line = Objects.toString(rawLine, "").trim();
    if (line.isEmpty()) return Optional.empty();

    if (line.startsWith("@")) {
      int space = line.indexOf(' ');
      if (space <= 0 || space + 1 >= line.length()) return Optional.empty();
      line = line.substring(space + 1).trim();
    }
    if (line.startsWith(":")) {
      int space = line.indexOf(' ');
      if (space <= 0 || space + 1 >= line.length()) return Optional.empty();
      line = line.substring(space + 1).trim();
    }

    int trailing = line.indexOf(" :");
    String head = trailing >= 0 ? line.substring(0, trailing).trim() : line;
    if (head.isEmpty()) return Optional.empty();

    String[] fields = head.split("\\s+");
    if (fields.length < 2 || !"005".equals(fields[0])) return Optional.empty();

    ArrayList<Token> parsed = new ArrayList<>();
    // fields[1] is the target nick. Everything after it is an ISUPPORT token.
    for (int i = 2; i < fields.length; i++) {
      String field = Objects.toString(fields[i], "").trim();
      if (field.isEmpty()) continue;

      boolean removed = field.startsWith("-") && field.length() > 1;
      if (removed) field = field.substring(1).trim();
      if (field.isEmpty()) continue;

      int equals = field.indexOf('=');
      String key = equals >= 0 ? field.substring(0, equals).trim() : field;
      String value = equals >= 0 ? field.substring(equals + 1).trim() : "";
      if (key.isEmpty()) continue;
      parsed.add(new Token(key, value, removed));
    }
    return Optional.of(new Ircv3IsupportLine(parsed));
  }

  /** Returns the final occurrence of a token, matching keys case-insensitively. */
  public Optional<Token> lastToken(String rawKey) {
    String key = normalizeKey(rawKey);
    if (key.isEmpty()) return Optional.empty();
    for (int i = tokens.size() - 1; i >= 0; i--) {
      Token token = tokens.get(i);
      if (token.normalizedKey().equals(key)) return Optional.of(token);
    }
    return Optional.empty();
  }

  public boolean hasEnabledToken(String rawKey) {
    return lastToken(rawKey).filter(token -> !token.removed()).isPresent();
  }

  private static String normalizeKey(String rawKey) {
    return Objects.toString(rawKey, "").trim().toUpperCase(Locale.ROOT);
  }

  public record Token(String key, String value, boolean removed) {
    public Token {
      key = Objects.toString(key, "").trim();
      value = Objects.toString(value, "").trim();
      if (key.isEmpty()) throw new IllegalArgumentException("ISUPPORT token key is required");
    }

    public String normalizedKey() {
      return normalizeKey(key);
    }
  }
}
