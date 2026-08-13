package cafe.woden.ircclient.irc.ircv3;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Transport-independent target metadata observed on an experimental message-edit tag. */
public record Ircv3MessageEditTagSignal(String targetMessageId) {

  private static final String DRAFT_EDIT = "draft/edit";

  public Ircv3MessageEditTagSignal {
    targetMessageId = Objects.toString(targetMessageId, "").trim();
    if (targetMessageId.isEmpty()) {
      throw new IllegalArgumentException("targetMessageId must not be blank");
    }
  }

  public static Optional<Ircv3MessageEditTagSignal> fromTags(Map<String, String> tags) {
    String targetMessageId = Ircv3Tags.firstDecodedTagValue(tags, DRAFT_EDIT);
    if (targetMessageId.isBlank()) return Optional.empty();
    return Optional.of(new Ircv3MessageEditTagSignal(targetMessageId));
  }
}
