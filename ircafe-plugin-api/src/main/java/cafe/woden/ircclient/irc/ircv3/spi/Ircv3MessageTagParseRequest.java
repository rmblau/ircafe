package cafe.woden.ircclient.irc.ircv3.spi;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Transport-neutral input supplied to an IRCv3 message-tag parser provider. */
public record Ircv3MessageTagParseRequest(Map<String, String> transportTags, String rawLine) {

  public Ircv3MessageTagParseRequest {
    LinkedHashMap<String, String> copied = new LinkedHashMap<>();
    for (Map.Entry<String, String> entry :
        Objects.requireNonNullElse(transportTags, Map.<String, String>of()).entrySet()) {
      if (entry.getKey() != null) {
        copied.put(entry.getKey(), Objects.toString(entry.getValue(), ""));
      }
    }
    transportTags = copied.isEmpty() ? Map.of() : Collections.unmodifiableMap(copied);
    rawLine = Objects.toString(rawLine, "");
  }
}
