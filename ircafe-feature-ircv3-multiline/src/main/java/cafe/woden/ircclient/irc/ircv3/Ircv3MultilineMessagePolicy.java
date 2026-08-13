package cafe.woden.ircclient.irc.ircv3;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Transport-independent normalization and negotiated-limit policy for IRCv3 multiline payloads. */
public final class Ircv3MultilineMessagePolicy {

  private Ircv3MultilineMessagePolicy() {}

  public static List<String> normalizeLines(String raw) {
    String input = Objects.toString(raw, "");
    if (input.isEmpty()) return List.of();

    String normalized = input.replace("\r\n", "\n").replace('\r', '\n');
    if (normalized.indexOf('\n') < 0) {
      return List.of(normalized);
    }

    String[] parts = normalized.split("\n", -1);
    List<String> out = new ArrayList<>(parts.length);
    for (String part : parts) {
      out.add(Objects.toString(part, ""));
    }
    return List.copyOf(out);
  }

  public static String joinLines(List<String> lines) {
    if (lines == null || lines.isEmpty()) return "";
    return String.join("\n", lines);
  }

  public static long payloadUtf8Bytes(List<String> lines) {
    if (lines == null || lines.isEmpty()) return 0L;

    long total = 0L;
    for (int i = 0; i < lines.size(); i++) {
      String line = Objects.toString(lines.get(i), "");
      total = addSaturated(total, line.getBytes(StandardCharsets.UTF_8).length);
      if (i < lines.size() - 1) {
        total = addSaturated(total, 1L);
      }
    }
    return total;
  }

  public static void requireWithinMaxBytes(long maxBytes, List<String> lines, String serverId) {
    if (maxBytes <= 0L) return;

    long payloadBytes = payloadUtf8Bytes(lines);
    if (payloadBytes <= maxBytes) return;

    throw new IllegalArgumentException(
        "Message exceeds negotiated IRCv3 multiline max-bytes "
            + payloadBytes
            + " > "
            + maxBytes
            + " for "
            + Objects.toString(serverId, "").trim());
  }

  public static void requireWithinMaxLines(long maxLines, List<String> lines, String serverId) {
    if (maxLines <= 0L) return;

    long lineCount = lines == null ? 0L : lines.size();
    if (lineCount <= maxLines) return;

    throw new IllegalArgumentException(
        "Message exceeds negotiated IRCv3 multiline max-lines "
            + lineCount
            + " > "
            + maxLines
            + " for "
            + Objects.toString(serverId, "").trim());
  }

  private static long addSaturated(long left, long right) {
    if (right <= 0L) return left;
    if (left >= Long.MAX_VALUE - right) return Long.MAX_VALUE;
    return left + right;
  }
}
