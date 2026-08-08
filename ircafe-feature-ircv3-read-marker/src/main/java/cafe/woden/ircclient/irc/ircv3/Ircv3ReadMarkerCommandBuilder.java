package cafe.woden.ircclient.irc.ircv3;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/** Formats read-marker timestamps and builds outbound MARKREAD commands. */
public final class Ircv3ReadMarkerCommandBuilder {

  private static final DateTimeFormatter TIMESTAMP_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC);

  private Ircv3ReadMarkerCommandBuilder() {}

  public static String formatTimestamp(Instant markerAt) {
    return TIMESTAMP_FORMATTER.format(markerAt == null ? Instant.now() : markerAt);
  }

  public static String buildTimestampRawLine(String target, Instant markerAt) {
    String outTarget = Ircv3CommandValuePolicy.normalizeTarget(target);
    if (outTarget.isEmpty()) return "";
    return "MARKREAD " + outTarget + " timestamp=" + formatTimestamp(markerAt);
  }
}
