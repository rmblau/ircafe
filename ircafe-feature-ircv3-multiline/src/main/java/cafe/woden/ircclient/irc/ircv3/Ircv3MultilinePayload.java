package cafe.woden.ircclient.irc.ircv3;

import java.util.List;
import java.util.Objects;

/** Immutable normalized view of one prospective IRCv3 multiline payload. */
public record Ircv3MultilinePayload(List<String> lines, String joinedText, long utf8Bytes) {

  public Ircv3MultilinePayload {
    lines = lines == null ? List.of() : List.copyOf(lines);
    joinedText = Objects.toString(joinedText, "");
    utf8Bytes = Math.max(0L, utf8Bytes);
  }

  public static Ircv3MultilinePayload from(String raw) {
    List<String> normalizedLines = Ircv3MultilineMessagePolicy.normalizeLines(raw);
    return new Ircv3MultilinePayload(
        normalizedLines,
        Ircv3MultilineMessagePolicy.joinLines(normalizedLines),
        Ircv3MultilineMessagePolicy.payloadUtf8Bytes(normalizedLines));
  }

  public int lineCount() {
    return lines.size();
  }

  public boolean isEmpty() {
    return lines.isEmpty();
  }

  public boolean isMultiline() {
    return lines.size() > 1;
  }
}
