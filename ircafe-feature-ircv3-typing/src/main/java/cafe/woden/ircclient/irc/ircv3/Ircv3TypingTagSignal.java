package cafe.woden.ircclient.irc.ircv3;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Transport-independent IRCv3 typing state observed on a tagged message. */
public record Ircv3TypingTagSignal(String state) {

  private static final String TYPING = "typing";

  public Ircv3TypingTagSignal {
    state = Objects.toString(state, "").trim();
    if (state.isEmpty()) throw new IllegalArgumentException("state must not be blank");
  }

  public static Optional<Ircv3TypingTagSignal> fromTags(Map<String, String> tags) {
    String state = Ircv3Tags.firstDecodedTagValue(tags, TYPING);
    if (state.isBlank()) return Optional.empty();
    return Optional.of(new Ircv3TypingTagSignal(state));
  }
}
