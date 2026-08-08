package cafe.woden.ircclient.irc.ircv3;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Transport-independent message-redaction metadata observed on legacy draft tags. */
public record Ircv3MessageRedactionTagSignal(String messageId) {

  private static final String DRAFT_DELETE = "draft/delete";
  private static final String DRAFT_REDACT = "draft/redact";

  public Ircv3MessageRedactionTagSignal {
    messageId = Objects.toString(messageId, "").trim();
    if (messageId.isEmpty()) throw new IllegalArgumentException("messageId must not be blank");
  }

  public static Optional<Ircv3MessageRedactionTagSignal> fromTags(Map<String, String> tags) {
    String messageId = Ircv3Tags.firstDecodedTagValue(tags, DRAFT_DELETE, DRAFT_REDACT);
    if (messageId.isBlank()) return Optional.empty();
    return Optional.of(new Ircv3MessageRedactionTagSignal(messageId));
  }
}
