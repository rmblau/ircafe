package cafe.woden.ircclient.irc.ircv3;

import java.time.Instant;
import java.util.Objects;

/** Parses IRCv3 read-marker timestamp values into epoch milliseconds. */
public final class Ircv3ReadMarkerTimestamp {

  private Ircv3ReadMarkerTimestamp() {}

  public static long parseEpochMs(String marker, Instant fallbackAt) {
    Instant fallback = fallbackAt != null ? fallbackAt : Instant.now();
    String raw = Objects.toString(marker, "").trim();
    if (raw.isEmpty() || "*".equals(raw)) return 0L;

    String value = raw;
    int equals = raw.indexOf('=');
    if (equals > 0 && equals < raw.length() - 1) {
      String key = raw.substring(0, equals).trim();
      if ("timestamp".equalsIgnoreCase(key)) {
        value = raw.substring(equals + 1).trim();
      }
    }
    if (value.isEmpty() || "*".equals(value)) return 0L;

    try {
      return Instant.parse(value).toEpochMilli();
    } catch (Exception ignored) {
    }

    try {
      long parsed = Long.parseLong(value);
      if (parsed <= 0L) return fallback.toEpochMilli();
      if (value.length() <= 10) return Math.multiplyExact(parsed, 1000L);
      return parsed;
    } catch (Exception ignored) {
      return fallback.toEpochMilli();
    }
  }
}
