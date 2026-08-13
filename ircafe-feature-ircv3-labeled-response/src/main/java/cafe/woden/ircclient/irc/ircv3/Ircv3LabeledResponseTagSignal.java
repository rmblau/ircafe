package cafe.woden.ircclient.irc.ircv3;

import java.util.Map;
import java.util.Optional;

/** Extracts the normalized {@code label=} value carried by an IRCv3 response. */
public final class Ircv3LabeledResponseTagSignal {

  private Ircv3LabeledResponseTagSignal() {}

  public static Optional<String> fromTags(Map<String, String> tags) {
    String label = Ircv3Tags.firstTagValue(tags, "label", "+label");
    label = Ircv3LabeledResponseValues.normalizeLabel(label);
    return label.isEmpty() ? Optional.empty() : Optional.of(label);
  }

  public static Optional<String> fromRawLine(String rawLine) {
    return fromTags(Ircv3Tags.fromRawLine(rawLine));
  }

  public static Outcome outcomeForStandardReply(String replyKind) {
    return "FAIL".equalsIgnoreCase(Ircv3LabeledResponseValues.normalizeLabel(replyKind))
        ? Outcome.FAILURE
        : Outcome.SUCCESS;
  }

  public enum Outcome {
    SUCCESS,
    FAILURE
  }
}
