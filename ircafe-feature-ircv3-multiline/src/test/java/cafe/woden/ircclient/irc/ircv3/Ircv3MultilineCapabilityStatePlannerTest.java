package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class Ircv3MultilineCapabilityStatePlannerTest {

  private final Ircv3MultilineCapabilityStatePlanner planner =
      new Ircv3MultilineCapabilityStatePlanner();

  @Test
  void offerThenAckWithoutValuesReusesOfferedLimits() {
    Ircv3MultilineCapabilityStatePlanner.State offered =
        planner.apply(
            Ircv3CapabilityLine.parse("LS", ":multiline=max-bytes=3072,max-lines=4"),
            Ircv3MultilineCapabilityStatePlanner.State.empty());
    Ircv3MultilineCapabilityStatePlanner.State acked =
        planner.apply(Ircv3CapabilityLine.parse("ACK", ":multiline"), offered);

    assertEquals(3072L, acked.multiline().offeredMaxBytes());
    assertEquals(4L, acked.multiline().offeredMaxLines());
    assertEquals(3072L, acked.multiline().negotiatedMaxBytes());
    assertEquals(4L, acked.multiline().negotiatedMaxLines());
  }

  @Test
  void explicitAckUpdatesFinalAndDraftLimits() {
    Ircv3MultilineCapabilityStatePlanner.State state =
        planner.apply(
            Ircv3CapabilityLine.parse(
                "ACK",
                ":multiline=max-bytes=4096,max-lines=5 "
                    + "draft/multiline=max-bytes=2048,max-lines=3"),
            Ircv3MultilineCapabilityStatePlanner.State.empty());

    assertEquals(4096L, state.multiline().negotiatedMaxBytes());
    assertEquals(5L, state.multiline().negotiatedMaxLines());
    assertEquals(2048L, state.draftMultiline().negotiatedMaxBytes());
    assertEquals(3L, state.draftMultiline().negotiatedMaxLines());
  }

  @Test
  void disabledOfferClearsOnlyOfferedValuesAndDelClearsEverything() {
    Ircv3MultilineCapabilityStatePlanner.State initial =
        new Ircv3MultilineCapabilityStatePlanner.State(
            new Ircv3MultilineCapabilityStatePlanner.Limits(4096, 5, 3072, 4), null);
    Ircv3MultilineCapabilityStatePlanner.State offeredDisabled =
        planner.apply(Ircv3CapabilityLine.parse("NEW", ":-multiline"), initial);

    assertEquals(0L, offeredDisabled.multiline().offeredMaxBytes());
    assertEquals(3072L, offeredDisabled.multiline().negotiatedMaxBytes());

    Ircv3MultilineCapabilityStatePlanner.State deleted =
        planner.apply(Ircv3CapabilityLine.parse("DEL", ":multiline"), offeredDisabled);
    assertEquals(0L, deleted.multiline().offeredMaxBytes());
    assertEquals(0L, deleted.multiline().negotiatedMaxBytes());
  }

  @Test
  void unrelatedCapabilitiesAndActionsLeaveStateUnchanged() {
    Ircv3MultilineCapabilityStatePlanner.State initial =
        new Ircv3MultilineCapabilityStatePlanner.State(
            new Ircv3MultilineCapabilityStatePlanner.Limits(10, 2, 8, 1), null);

    assertEquals(initial, planner.apply(Ircv3CapabilityLine.parse("NAK", ":multiline"), initial));
    assertEquals(
        initial, planner.apply(Ircv3CapabilityLine.parse("ACK", ":message-tags"), initial));
  }
}
