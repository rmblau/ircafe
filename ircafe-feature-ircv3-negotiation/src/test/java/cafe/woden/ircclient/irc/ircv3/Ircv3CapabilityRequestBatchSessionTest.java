package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class Ircv3CapabilityRequestBatchSessionTest {

  @Test
  void requestsOfferedCapabilitiesInDesiredOrderAndTracksResolution() {
    Ircv3CapabilityRequestBatchSession session =
        new Ircv3CapabilityRequestBatchSession(
            List.of(" multi-prefix ", "MULTI-PREFIX", "server-time", "batch"));

    Ircv3CapabilityRequestBatchSession.LsDecision decision =
        session.observeLs(List.of("batch=max-bytes=4096", "server-time", "sasl"));

    assertEquals(List.of("server-time", "batch"), decision.capabilitiesToRequest());
    assertFalse(decision.finished());
    assertTrue(session.isPending("batch"));
    assertFalse(session.resolve(List.of(":server-time")));
    assertTrue(session.resolve(List.of("-batch")));
  }

  @Test
  void waitsForFinalLsAfterContinuationMarker() {
    Ircv3CapabilityRequestBatchSession session =
        new Ircv3CapabilityRequestBatchSession(List.of("message-tags"));

    Ircv3CapabilityRequestBatchSession.LsDecision decision = session.observeLs(List.of(":*"));

    assertTrue(decision.capabilitiesToRequest().isEmpty());
    assertFalse(decision.finished());
  }

  @Test
  void finishesWhenNothingDesiredIsOffered() {
    Ircv3CapabilityRequestBatchSession session =
        new Ircv3CapabilityRequestBatchSession(List.of("message-tags"));

    Ircv3CapabilityRequestBatchSession.LsDecision decision =
        session.observeLs(List.of("batch", "sasl"));

    assertTrue(decision.capabilitiesToRequest().isEmpty());
    assertTrue(decision.finished());
  }
}
