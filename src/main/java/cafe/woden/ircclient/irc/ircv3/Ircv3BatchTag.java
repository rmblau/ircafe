package cafe.woden.ircclient.irc.ircv3;

import static cafe.woden.ircclient.util.Ircv3CapabilityNames.BATCH;

import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Ircv3BatchTag {

  public static Optional<String> fromEvent(Object pircbotxEvent) {
    if (pircbotxEvent == null) return Optional.empty();

    String batch = Ircv3Tags.firstTagValue(Ircv3Tags.fromEvent(pircbotxEvent), BATCH);
    return batch.isBlank() ? Optional.empty() : Optional.of(batch);
  }

  /** Parse {@code @batch=} from a raw IRC line. */
  public static Optional<String> fromRawLine(String rawLine) {
    String batch = Ircv3Tags.firstTagValue(Ircv3Tags.fromRawLine(rawLine), BATCH);
    return batch.isBlank() ? Optional.empty() : Optional.of(batch);
  }
}
