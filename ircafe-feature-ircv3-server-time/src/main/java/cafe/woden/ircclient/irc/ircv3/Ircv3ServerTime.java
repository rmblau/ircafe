package cafe.woden.ircclient.irc.ircv3;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

public final class Ircv3ServerTime {

  private Ircv3ServerTime() {}

  public static Optional<Instant> fromEvent(Object pircbotxEvent) {
    if (pircbotxEvent == null) return Optional.empty();
    return fromTags(Ircv3Tags.fromEvent(pircbotxEvent));
  }

  public static Optional<Instant> fromTags(Map<String, String> tags) {
    String rawTime = Ircv3Tags.firstTagValue(tags, "time");
    return rawTime.isBlank() ? Optional.empty() : parseInstantSafe(rawTime);
  }

  /** Parse {@code @time=} tag from a raw IRC line. */
  public static Optional<Instant> fromRawLine(String rawLine) {
    Map<String, String> tags = Ircv3Tags.fromRawLine(rawLine);
    String rawTime = Ircv3Tags.firstTagValue(tags, "time");
    if (rawTime.isBlank()) return Optional.empty();
    return parseInstantSafe(rawTime);
  }

  public static Optional<Instant> fromTagsOrRawLine(Map<String, String> tags, String rawLine) {
    Optional<Instant> tagged = fromTags(tags);
    return tagged.isPresent() ? tagged : fromRawLine(rawLine);
  }

  public static Instant parseServerTimeFromRawLine(String rawLine) {
    return fromRawLine(rawLine).orElse(null);
  }

  public static Instant orNow(Optional<Instant> maybe, Instant fallbackNow) {
    return maybe != null && maybe.isPresent() ? maybe.get() : fallbackNow;
  }

  private static Optional<Instant> parseInstantSafe(String raw) {
    if (raw == null) return Optional.empty();
    String v = raw.trim();
    if (v.isEmpty()) return Optional.empty();
    try {
      return Optional.of(Instant.parse(v));
    } catch (Exception ignored) {
      return Optional.empty();
    }
  }
}
