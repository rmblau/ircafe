package cafe.woden.ircclient.irc.ircv3;

import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignal;
import java.util.List;
import java.util.Objects;

/** Validates runtime-provider results for multiline CAP limit transitions. */
public final class Ircv3MultilineCapabilityRuntimeSupport {

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

    public static Limits empty() {
      return new Limits(0L, 0L, 0L, 0L);
    }
  }

  public record State(Limits multiline, Limits draftMultiline) {
    public State {
      multiline = multiline == null ? Limits.empty() : multiline;
      draftMultiline = draftMultiline == null ? Limits.empty() : draftMultiline;
    }

    public static State empty() {
      return new State(Limits.empty(), Limits.empty());
    }
  }

  private final Ircv3InboundCommandSignalRuntimeCatalog runtimeCatalog;

  public Ircv3MultilineCapabilityRuntimeSupport(
      Ircv3InboundCommandSignalRuntimeCatalog runtimeCatalog) {
    this.runtimeCatalog = Objects.requireNonNull(runtimeCatalog, "runtimeCatalog");
  }

  public State apply(Ircv3CapabilityLine line, State current) {
    Objects.requireNonNull(line, "line");
    State prior = current == null ? State.empty() : current;
    if (!line.hasTokens() || !line.isAction("LS", "NEW", "ACK", "DEL")) {
      return prior;
    }

    List<Ircv3InboundCommandSignal> signals =
        runtimeCatalog.parse(
            Ircv3InboundCommandOperation.MULTILINE_CAPABILITY_STATE,
            Ircv3InboundCommandRequest.multilineCapabilityState(
                line.action(), line.normalizedCaps(), toPortable(prior)));

    Limits multiline = prior.multiline();
    Limits draft = prior.draftMultiline();
    boolean sawMultiline = false;
    boolean sawDraft = false;
    for (Ircv3InboundCommandSignal signal : signals) {
      if (!(signal instanceof Ircv3InboundCommandSignal.MultilineLimitsObserved observed)) {
        continue;
      }
      Limits limits =
          new Limits(
              observed.offeredMaxBytes(),
              observed.offeredMaxLines(),
              observed.negotiatedMaxBytes(),
              observed.negotiatedMaxLines());
      if (observed.draftCapability()) {
        if (sawDraft) {
          throw new IllegalStateException(
              "Multiline runtime provider returned duplicate draft limit observations");
        }
        sawDraft = true;
        draft = limits;
      } else {
        if (sawMultiline) {
          throw new IllegalStateException(
              "Multiline runtime provider returned duplicate final limit observations");
        }
        sawMultiline = true;
        multiline = limits;
      }
    }
    return new State(multiline, draft);
  }

  private static Ircv3InboundCommandRequest.MultilineState toPortable(State state) {
    return new Ircv3InboundCommandRequest.MultilineState(
        state.multiline().offeredMaxBytes(),
        state.multiline().offeredMaxLines(),
        state.multiline().negotiatedMaxBytes(),
        state.multiline().negotiatedMaxLines(),
        state.draftMultiline().offeredMaxBytes(),
        state.draftMultiline().offeredMaxLines(),
        state.draftMultiline().negotiatedMaxBytes(),
        state.draftMultiline().negotiatedMaxLines());
  }
}
