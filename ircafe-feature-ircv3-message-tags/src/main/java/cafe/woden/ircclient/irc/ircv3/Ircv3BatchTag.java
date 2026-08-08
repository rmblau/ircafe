package cafe.woden.ircclient.irc.ircv3;

import java.util.Map;
import java.util.Optional;

public final class Ircv3BatchTag {

  private static final String BATCH_TAG = "batch";

  private Ircv3BatchTag() {}

  public static Optional<String> fromEvent(Object pircbotxEvent) {
    if (pircbotxEvent == null) return Optional.empty();
    return fromTags(Ircv3Tags.fromEvent(pircbotxEvent));
  }

  public static Optional<String> fromTags(Map<String, String> tags) {
    String batch = Ircv3Tags.firstTagValue(tags, BATCH_TAG);
    return batch.isBlank() ? Optional.empty() : Optional.of(batch);
  }

  /** Parse {@code @batch=} from a raw IRC line. */
  public static Optional<String> fromRawLine(String rawLine) {
    return fromTags(Ircv3Tags.fromRawLine(rawLine));
  }
}
