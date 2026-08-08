package cafe.woden.ircclient.irc.ircv3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/** Parsed transport-independent representation of a staged {@code /quote @tags COMMAND} draft. */
public final class Ircv3TaggedCommandDraft {

  private static final String QUOTE_COMMAND = "/quote";

  private final String original;
  private final String leadingWhitespace;
  private final String commandPrefix;
  private final List<Tag> tags;
  private final String commandTail;

  private Ircv3TaggedCommandDraft(
      String original,
      String leadingWhitespace,
      String commandPrefix,
      List<Tag> tags,
      String commandTail) {
    this.original = original;
    this.leadingWhitespace = leadingWhitespace;
    this.commandPrefix = commandPrefix;
    this.tags = List.copyOf(tags);
    this.commandTail = commandTail;
  }

  public static Optional<Ircv3TaggedCommandDraft> parse(String draft) {
    String raw = Objects.toString(draft, "");
    if (raw.isBlank()) return Optional.empty();

    int whitespaceEnd = 0;
    while (whitespaceEnd < raw.length() && Character.isWhitespace(raw.charAt(whitespaceEnd))) {
      whitespaceEnd++;
    }
    String leadingWhitespace = raw.substring(0, whitespaceEnd);
    String rest = raw.substring(whitespaceEnd);

    if (!startsWithIgnoreCase(rest, QUOTE_COMMAND)) return Optional.empty();
    int cursor = QUOTE_COMMAND.length();
    if (rest.length() > cursor && !Character.isWhitespace(rest.charAt(cursor))) {
      return Optional.empty();
    }
    while (cursor < rest.length() && Character.isWhitespace(rest.charAt(cursor))) cursor++;
    if (cursor >= rest.length() || rest.charAt(cursor) != '@') return Optional.empty();

    int tagStart = cursor;
    int tagEnd = firstWhitespaceAtOrAfter(rest, tagStart);
    if (tagEnd < 0) return Optional.empty();

    List<Tag> tags = parseTags(rest.substring(tagStart + 1, tagEnd));
    if (tags.isEmpty()) return Optional.empty();

    return Optional.of(
        new Ircv3TaggedCommandDraft(
            raw,
            leadingWhitespace,
            rest.substring(0, tagStart),
            tags,
            rest.substring(tagEnd)));
  }

  public boolean hasAnyTag(String... tagKeys) {
    Set<String> normalizedKeys = normalizeKeys(tagKeys);
    return tags.stream().anyMatch(tag -> normalizedKeys.contains(tag.normalizedKey()));
  }

  public String withoutTags(String... tagKeys) {
    Set<String> normalizedKeys = normalizeKeys(tagKeys);
    List<Tag> kept =
        tags.stream()
            .filter(tag -> !normalizedKeys.contains(tag.normalizedKey()))
            .toList();
    if (kept.size() == tags.size()) return original;

    if (kept.isEmpty()) {
      String command = commandTail.stripLeading();
      if (command.isEmpty()) return "";
      return leadingWhitespace + commandPrefix + command;
    }

    String tagSection = kept.stream().map(Tag::source).collect(Collectors.joining(";"));
    return leadingWhitespace + commandPrefix + "@" + tagSection + commandTail;
  }

  private static List<Tag> parseTags(String tagSection) {
    String[] parts = Objects.toString(tagSection, "").split(";");
    ArrayList<Tag> parsed = new ArrayList<>(parts.length);
    for (String part : parts) {
      String source = Objects.toString(part, "");
      if (source.trim().isEmpty()) continue;
      parsed.add(new Tag(source, normalizeTagKey(source)));
    }
    return parsed;
  }

  private static Set<String> normalizeKeys(String... tagKeys) {
    return Arrays.stream(Objects.requireNonNullElseGet(tagKeys, () -> new String[0]))
        .map(Ircv3TaggedCommandDraft::normalizeTagKey)
        .filter(key -> !key.isEmpty())
        .collect(Collectors.toUnmodifiableSet());
  }

  private static String normalizeTagKey(String tagPart) {
    String token = Objects.toString(tagPart, "");
    int equals = token.indexOf('=');
    if (equals >= 0) token = token.substring(0, equals);
    token = token.trim();
    while (token.startsWith("+")) token = token.substring(1);
    return token.toLowerCase(Locale.ROOT);
  }

  private static int firstWhitespaceAtOrAfter(String value, int start) {
    for (int i = Math.max(0, start); i < value.length(); i++) {
      if (Character.isWhitespace(value.charAt(i))) return i;
    }
    return -1;
  }

  private static boolean startsWithIgnoreCase(String value, String prefix) {
    if (value == null || prefix == null || value.length() < prefix.length()) return false;
    return value.regionMatches(true, 0, prefix, 0, prefix.length());
  }

  private record Tag(String source, String normalizedKey) {}
}
