package cafe.woden.ircclient.irc.ircv3;

import java.util.Objects;

/** Applies multiline CAP offers/ACK/DEL tokens to transport-neutral limit state. */
public final class Ircv3MultilineCapabilityStatePlanner {

  public record Limits(
      long offeredMaxBytes,
      long offeredMaxLines,
      long negotiatedMaxBytes,
      long negotiatedMaxLines) {

    public Limits {
      offeredMaxBytes = Math.max(0L, offeredMaxBytes);
      offeredMaxLines = Math.max(0L, offeredMaxLines);
      negotiatedMaxBytes = Math.max(0L, negotiatedMaxBytes);
      negotiatedMaxLines = Math.max(0L, negotiatedMaxLines);
    }
  }

  public record State(Limits multiline, Limits draftMultiline) {
    public State {
      multiline = multiline == null ? emptyLimits() : multiline;
      draftMultiline = draftMultiline == null ? emptyLimits() : draftMultiline;
    }

    public static State empty() {
      return new State(emptyLimits(), emptyLimits());
    }

    private static Limits emptyLimits() {
      return new Limits(0L, 0L, 0L, 0L);
    }
  }

  public State apply(Ircv3CapabilityLine line, State current) {
    Objects.requireNonNull(line, "line");
    Objects.requireNonNull(current, "current");
    boolean fromOffer = line.isAction("LS", "NEW");
    boolean fromAck = line.isAction("ACK");
    boolean fromDel = line.isAction("DEL");
    if (!fromOffer && !fromAck && !fromDel) return current;

    Limits multiline = current.multiline();
    Limits draft = current.draftMultiline();
    for (String rawToken : line.tokens()) {
      Ircv3CapabilityToken token = Ircv3CapabilityToken.parse(rawToken).orElse(null);
      if (token == null || !Ircv3MultilineSupport.isMultilineCapability(token.name())) continue;

      boolean draftCapability =
          Ircv3MultilineSupport.isDraftMultilineCapability(token.normalizedName());
      Limits prior = draftCapability ? draft : multiline;
      Limits next = applyToken(prior, token, fromOffer, fromAck, fromDel);
      if (draftCapability) {
        draft = next;
      } else {
        multiline = next;
      }
    }
    return new State(multiline, draft);
  }

  private static Limits applyToken(
      Limits prior,
      Ircv3CapabilityToken token,
      boolean fromOffer,
      boolean fromAck,
      boolean fromDel) {
    if (fromDel || (fromAck && token.disabled())) {
      return new Limits(0L, 0L, 0L, 0L);
    }

    Ircv3MultilineSupport.LimitParams parsed =
        Ircv3MultilineSupport.parseLimitParams(token.value());
    long parsedBytes = Math.max(0L, parsed.maxBytes());
    long parsedLines = Math.max(0L, parsed.maxLines());
    long offeredBytes = prior.offeredMaxBytes();
    long offeredLines = prior.offeredMaxLines();

    if (fromOffer) {
      if (token.disabled()) {
        offeredBytes = 0L;
        offeredLines = 0L;
      } else {
        if (parsedBytes > 0L) offeredBytes = parsedBytes;
        if (parsedLines > 0L) offeredLines = parsedLines;
      }
      return new Limits(
          offeredBytes, offeredLines, prior.negotiatedMaxBytes(), prior.negotiatedMaxLines());
    }

    if (fromAck) {
      if (parsedBytes > 0L) offeredBytes = parsedBytes;
      if (parsedLines > 0L) offeredLines = parsedLines;
      long negotiatedBytes = parsedBytes > 0L ? parsedBytes : offeredBytes;
      long negotiatedLines = parsedLines > 0L ? parsedLines : offeredLines;
      return new Limits(offeredBytes, offeredLines, negotiatedBytes, negotiatedLines);
    }

    return prior;
  }
}
