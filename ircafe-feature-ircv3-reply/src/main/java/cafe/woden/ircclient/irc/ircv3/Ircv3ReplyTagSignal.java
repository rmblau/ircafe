package cafe.woden.ircclient.irc.ircv3;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Transport-independent reply metadata observed on an IRCv3 tagged message. */
public record Ircv3ReplyTagSignal(String replyToMessageId) {

  private static final String REPLY = "reply";
  private static final String DRAFT_REPLY = "draft/reply";

  public Ircv3ReplyTagSignal {
    replyToMessageId = Objects.toString(replyToMessageId, "").trim();
    if (replyToMessageId.isEmpty()) {
      throw new IllegalArgumentException("replyToMessageId must not be blank");
    }
  }

  public static Optional<Ircv3ReplyTagSignal> fromTags(Map<String, String> tags) {
    String replyTo = Ircv3Tags.firstDecodedTagValue(tags, REPLY, DRAFT_REPLY);
    if (replyTo.isBlank()) return Optional.empty();
    return Optional.of(new Ircv3ReplyTagSignal(replyTo));
  }
}
