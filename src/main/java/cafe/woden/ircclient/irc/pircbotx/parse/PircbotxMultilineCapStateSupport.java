package cafe.woden.ircclient.irc.pircbotx.parse;

import cafe.woden.ircclient.irc.ircv3.Ircv3CapabilityLine;
import cafe.woden.ircclient.irc.ircv3.Ircv3CapabilitySnapshot;
import cafe.woden.ircclient.irc.ircv3.Ircv3MultilineCapabilityRuntimeSupport;
import cafe.woden.ircclient.irc.pircbotx.state.PircbotxConnectionState;
import java.util.Objects;

/** Applies runtime-planned multiline capability limit transitions to connection state. */
public final class PircbotxMultilineCapStateSupport {

  private final Ircv3MultilineCapabilityRuntimeSupport runtimeSupport;

  public PircbotxMultilineCapStateSupport(Ircv3MultilineCapabilityRuntimeSupport runtimeSupport) {
    this.runtimeSupport = Objects.requireNonNull(runtimeSupport, "runtimeSupport");
  }

  public void observe(Ircv3CapabilityLine capLine, PircbotxConnectionState conn) {
    Ircv3CapabilitySnapshot snapshot = conn.capabilitySnapshot();
    Ircv3MultilineCapabilityRuntimeSupport.State next =
        runtimeSupport.apply(
            capLine,
            new Ircv3MultilineCapabilityRuntimeSupport.State(
                new Ircv3MultilineCapabilityRuntimeSupport.Limits(
                    conn.multilineOfferedMaxBytes(false),
                    conn.multilineOfferedMaxLines(false),
                    snapshot.multilineMaxBytes(),
                    snapshot.multilineMaxLines()),
                new Ircv3MultilineCapabilityRuntimeSupport.Limits(
                    conn.multilineOfferedMaxBytes(true),
                    conn.multilineOfferedMaxLines(true),
                    snapshot.draftMultilineMaxBytes(),
                    snapshot.draftMultilineMaxLines())));
    apply(conn, false, next.multiline());
    apply(conn, true, next.draftMultiline());
  }

  private static void apply(
      PircbotxConnectionState conn,
      boolean draft,
      Ircv3MultilineCapabilityRuntimeSupport.Limits limits) {
    conn.setMultilineOfferedMaxBytes(draft, limits.offeredMaxBytes());
    conn.setMultilineOfferedMaxLines(draft, limits.offeredMaxLines());
    conn.setNegotiatedMultilineMaxBytes(draft, limits.negotiatedMaxBytes());
    conn.setNegotiatedMultilineMaxLines(draft, limits.negotiatedMaxLines());
  }
}
