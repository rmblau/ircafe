package cafe.woden.ircclient.irc.ircv3;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** Plans raw IRC lines for one PRIVMSG or NOTICE payload. */
public final class Ircv3MultilineCommandPlanner {

  private Ircv3MultilineCommandPlanner() {}

  public record NegotiatedState(
      boolean multilineNegotiated,
      boolean draftMultilineNegotiated,
      long maxBytes,
      long maxLines) {}

  public record Plan(List<String> rawLines, boolean batched) {
    public Plan {
      rawLines = rawLines == null ? List.of() : List.copyOf(rawLines);
    }
  }

  public static Plan plan(
      String command,
      String target,
      String message,
      NegotiatedState negotiated,
      String batchId,
      String serverId) {
    Ircv3MultilinePayload payload = Ircv3MultilinePayload.from(message);
    if (payload.isEmpty()) {
      return new Plan(List.of(), false);
    }
    String normalizedCommand = normalizeCommand(command);
    String normalizedTarget = requireToken(target, "target");
    if (!payload.isMultiline()) {
      return new Plan(
          List.of(normalizedCommand + " " + normalizedTarget + " :" + payload.lines().getFirst()),
          false);
    }

    NegotiatedState state =
        negotiated == null ? new NegotiatedState(false, false, 0L, 0L) : negotiated;
    String batchType =
        Ircv3MultilineSupport.negotiatedBatchType(
            state.multilineNegotiated(), state.draftMultilineNegotiated());
    String concatTag =
        Ircv3MultilineSupport.negotiatedConcatTag(
            state.multilineNegotiated(), state.draftMultilineNegotiated());
    if (batchType.isEmpty() || concatTag.isEmpty()) {
      throw new IllegalArgumentException(
          "Message contains line breaks, but IRCv3 multiline is not negotiated: "
              + Objects.toString(serverId, "").trim());
    }

    Ircv3MultilineMessagePolicy.requireWithinMaxLines(
        state.maxLines(), payload.lines(), serverId);
    Ircv3MultilineMessagePolicy.requireWithinMaxBytes(
        state.maxBytes(), payload.lines(), serverId);

    String normalizedBatchId = requireToken(batchId, "batch id");
    List<String> rawLines = new ArrayList<>(payload.lineCount() + 2);
    rawLines.add(
        "BATCH +" + normalizedBatchId + " " + batchType + " " + normalizedTarget);
    for (int i = 0; i < payload.lineCount(); i++) {
      String tagPrefix = "@batch=" + normalizedBatchId;
      if (i < payload.lineCount() - 1) {
        tagPrefix += ";+" + concatTag + "=1";
      }
      rawLines.add(
          tagPrefix
              + " "
              + normalizedCommand
              + " "
              + normalizedTarget
              + " :"
              + payload.lines().get(i));
    }
    rawLines.add("BATCH -" + normalizedBatchId);
    return new Plan(rawLines, true);
  }

  private static String normalizeCommand(String command) {
    String normalized = requireToken(command, "command").toUpperCase(Locale.ROOT);
    if (!"PRIVMSG".equals(normalized) && !"NOTICE".equals(normalized)) {
      throw new IllegalArgumentException("Unsupported message command: " + command);
    }
    return normalized;
  }

  private static String requireToken(String raw, String label) {
    String normalized = Ircv3CommandValuePolicy.normalizeToken(raw);
    if (normalized.isEmpty()) {
      throw new IllegalArgumentException(label + " is blank or contains whitespace");
    }
    return normalized;
  }
}
