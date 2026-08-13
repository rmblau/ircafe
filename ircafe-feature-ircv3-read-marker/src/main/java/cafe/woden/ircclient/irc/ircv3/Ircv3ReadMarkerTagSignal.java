package cafe.woden.ircclient.irc.ircv3;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Transport-independent IRCv3 read-marker metadata observed on a tagged message. */
public record Ircv3ReadMarkerTagSignal(String marker) {

  private static final String READ_MARKER = "read-marker";
  private static final String DRAFT_READ_MARKER = "draft/read-marker";

  public Ircv3ReadMarkerTagSignal {
    marker = Objects.toString(marker, "").trim();
    if (marker.isEmpty()) throw new IllegalArgumentException("marker must not be blank");
  }

  public static Optional<Ircv3ReadMarkerTagSignal> fromTags(Map<String, String> tags) {
    String marker = Ircv3Tags.firstDecodedTagValue(tags, DRAFT_READ_MARKER, READ_MARKER);
    if (marker.isBlank()) return Optional.empty();
    return Optional.of(new Ircv3ReadMarkerTagSignal(marker));
  }
}
