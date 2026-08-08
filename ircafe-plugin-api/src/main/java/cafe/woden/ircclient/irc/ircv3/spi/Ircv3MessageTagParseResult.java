package cafe.woden.ircclient.irc.ircv3.spi;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Normalized IRCv3 message tags returned by a runtime parser provider. */
public record Ircv3MessageTagParseResult(Map<String, String> tags) {

  public Ircv3MessageTagParseResult {
    LinkedHashMap<String, String> copied = new LinkedHashMap<>();
    for (Map.Entry<String, String> entry :
        Objects.requireNonNullElse(tags, Map.<String, String>of()).entrySet()) {
      if (entry.getKey() != null) {
        copied.put(entry.getKey(), Objects.toString(entry.getValue(), ""));
      }
    }
    tags = copied.isEmpty() ? Map.of() : Collections.unmodifiableMap(copied);
  }

  public static Ircv3MessageTagParseResult empty() {
    return new Ircv3MessageTagParseResult(Map.of());
  }
}
