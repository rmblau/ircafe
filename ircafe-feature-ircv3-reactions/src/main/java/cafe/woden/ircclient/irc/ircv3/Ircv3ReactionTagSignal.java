package cafe.woden.ircclient.irc.ircv3;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Transport-independent reaction or reaction-removal metadata from IRCv3 message tags. */
public record Ircv3ReactionTagSignal(Operation operation, String reaction, String messageId) {

  private static final String DRAFT_REACT = "draft/react";
  private static final String DRAFT_UNREACT = "draft/unreact";
  private static final String REPLY = "reply";
  private static final String DRAFT_REPLY = "draft/reply";
  private static final String MSGID = "msgid";
  private static final String DRAFT_MSGID = "draft/msgid";

  public enum Operation {
    REACT,
    UNREACT
  }

  public Ircv3ReactionTagSignal {
    operation = Objects.requireNonNull(operation, "operation");
    reaction = Objects.toString(reaction, "").trim();
    messageId = Objects.toString(messageId, "").trim();
    if (reaction.isEmpty()) throw new IllegalArgumentException("reaction must not be blank");
  }

  public static List<Ircv3ReactionTagSignal> fromTags(Map<String, String> tags) {
    String messageId = observedMessageId(tags);
    ArrayList<Ircv3ReactionTagSignal> signals = new ArrayList<>(2);

    String react = Ircv3Tags.firstDecodedTagValue(tags, DRAFT_REACT);
    if (!react.isBlank()) {
      signals.add(new Ircv3ReactionTagSignal(Operation.REACT, react, messageId));
    }

    String unreact = Ircv3Tags.firstDecodedTagValue(tags, DRAFT_UNREACT);
    if (!unreact.isBlank()) {
      signals.add(new Ircv3ReactionTagSignal(Operation.UNREACT, unreact, messageId));
    }

    return List.copyOf(signals);
  }

  private static String observedMessageId(Map<String, String> tags) {
    String replyTo = Ircv3Tags.firstDecodedTagValue(tags, REPLY, DRAFT_REPLY);
    if (!replyTo.isBlank()) return replyTo;
    return Ircv3Tags.firstDecodedTagValue(tags, MSGID, DRAFT_MSGID);
  }
}
