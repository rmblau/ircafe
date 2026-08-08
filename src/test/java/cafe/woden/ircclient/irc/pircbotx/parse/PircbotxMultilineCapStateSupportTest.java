package cafe.woden.ircclient.irc.pircbotx.parse;

import static org.junit.jupiter.api.Assertions.assertEquals;

import cafe.woden.ircclient.irc.ircv3.Ircv3CapabilityLine;
import cafe.woden.ircclient.irc.ircv3.Ircv3InboundCommandSignalRuntimeCatalog;
import cafe.woden.ircclient.irc.ircv3.Ircv3MultilineCapabilityRuntimeSupport;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignal;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignalProvider;
import cafe.woden.ircclient.irc.pircbotx.state.PircbotxConnectionState;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class PircbotxMultilineCapStateSupportTest {

  private final PircbotxMultilineCapStateSupport support =
      PircbotxParserRuntimeTestFixtures.multiline();

  @Test
  void ackWithExplicitLimitsUpdatesNegotiatedAndOfferedValues() {
    PircbotxConnectionState conn = new PircbotxConnectionState("libera");

    support.observe(
        Ircv3CapabilityLine.parse(
            "ACK",
            ":multiline=max-bytes=4096,max-lines=5 draft/multiline=max-bytes=2048,max-lines=3"),
        conn);

    assertEquals(4096L, conn.multilineOfferedMaxBytes(false));
    assertEquals(5L, conn.multilineOfferedMaxLines(false));
    assertEquals(4096L, conn.capabilitySnapshot().multilineMaxBytes());
    assertEquals(5L, conn.capabilitySnapshot().multilineMaxLines());
    assertEquals(2048L, conn.multilineOfferedMaxBytes(true));
    assertEquals(3L, conn.multilineOfferedMaxLines(true));
    assertEquals(2048L, conn.capabilitySnapshot().draftMultilineMaxBytes());
    assertEquals(3L, conn.capabilitySnapshot().draftMultilineMaxLines());
  }

  @Test
  void ackWithoutExplicitLimitsReusesPreviouslyOfferedValues() {
    PircbotxConnectionState conn = new PircbotxConnectionState("libera");
    support.observe(Ircv3CapabilityLine.parse("LS", ":multiline=max-bytes=3072,max-lines=4"), conn);

    support.observe(Ircv3CapabilityLine.parse("ACK", ":multiline"), conn);

    assertEquals(3072L, conn.capabilitySnapshot().multilineMaxBytes());
    assertEquals(4L, conn.capabilitySnapshot().multilineMaxLines());
  }

  @Test
  void delClearsOnlyTheSelectedCapabilityVariant() {
    PircbotxConnectionState conn = new PircbotxConnectionState("libera");
    support.observe(
        Ircv3CapabilityLine.parse(
            "ACK",
            ":multiline=max-bytes=3072,max-lines=4 "
                + "draft/multiline=max-bytes=2048,max-lines=3"),
        conn);

    support.observe(Ircv3CapabilityLine.parse("DEL", ":multiline"), conn);

    assertEquals(0L, conn.multilineOfferedMaxBytes(false));
    assertEquals(0L, conn.multilineOfferedMaxLines(false));
    assertEquals(0L, conn.capabilitySnapshot().multilineMaxBytes());
    assertEquals(0L, conn.capabilitySnapshot().multilineMaxLines());
    assertEquals(2048L, conn.multilineOfferedMaxBytes(true));
    assertEquals(3L, conn.multilineOfferedMaxLines(true));
    assertEquals(2048L, conn.capabilitySnapshot().draftMultilineMaxBytes());
    assertEquals(3L, conn.capabilitySnapshot().draftMultilineMaxLines());
  }

  @Test
  void installedProviderCanOverrideLimitPlanning() {
    Ircv3InboundCommandSignalProvider provider =
        new Ircv3InboundCommandSignalProvider() {
          @Override
          public String providerId() {
            return "override";
          }

          @Override
          public int inboundCommandPriority() {
            return 100;
          }

          @Override
          public Set<Ircv3InboundCommandOperation> inboundCommandOperations() {
            return Set.of(Ircv3InboundCommandOperation.MULTILINE_CAPABILITY_STATE);
          }

          @Override
          public List<Ircv3InboundCommandSignal> parse(
              Ircv3InboundCommandOperation operation, Ircv3InboundCommandRequest request) {
            return List.of(
                new Ircv3InboundCommandSignal.MultilineLimitsObserved(
                    false, 8192L, 9L, 8192L, 9L),
                new Ircv3InboundCommandSignal.MultilineLimitsObserved(
                    true, 4096L, 4L, 4096L, 4L));
          }
        };
    PircbotxMultilineCapStateSupport overridden =
        new PircbotxMultilineCapStateSupport(
            new Ircv3MultilineCapabilityRuntimeSupport(
                Ircv3InboundCommandSignalRuntimeCatalog.fromProviders(List.of(provider))));
    PircbotxConnectionState conn = new PircbotxConnectionState("libera");

    overridden.observe(Ircv3CapabilityLine.parse("ACK", ":multiline"), conn);

    assertEquals(8192L, conn.capabilitySnapshot().multilineMaxBytes());
    assertEquals(9L, conn.capabilitySnapshot().multilineMaxLines());
    assertEquals(4096L, conn.capabilitySnapshot().draftMultilineMaxBytes());
    assertEquals(4L, conn.capabilitySnapshot().draftMultilineMaxLines());
  }
}
