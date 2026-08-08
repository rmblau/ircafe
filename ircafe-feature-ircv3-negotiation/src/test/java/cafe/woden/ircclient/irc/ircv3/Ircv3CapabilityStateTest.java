package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class Ircv3CapabilityStateTest {

  @Test
  void tracksStableAndDraftCapabilityAliases() {
    Ircv3CapabilityState state = new Ircv3CapabilityState();

    assertTrue(state.updateTrackedCapability("draft/chathistory", true));
    assertTrue(state.updateTrackedCapability("message-edit", true));
    assertFalse(state.updateTrackedCapability("draft/message-edit", true));
    assertFalse(state.updateTrackedCapability("unknown-capability", true));

    Ircv3CapabilitySnapshot snapshot = state.snapshot();
    assertTrue(snapshot.chatHistoryCapAcked());
    assertTrue(snapshot.draftMessageEditCapAcked());
  }

  @Test
  void prefersFinalMultilineLimitsAndClearsNegotiatedLimitsOnDisable() {
    Ircv3CapabilityState state = new Ircv3CapabilityState();
    state.setDraftMultilineCapAcked(true);
    state.setDraftMultilineLimits(2048L, 3L);
    state.setMultilineCapAcked(true);
    state.setMultilineLimits(4096L, 5L);

    Ircv3CapabilitySnapshot snapshot = state.snapshot();
    assertTrue(snapshot.multilineAvailable());
    assertEquals(4096L, snapshot.negotiatedMultilineMaxBytes());
    assertEquals(5L, snapshot.negotiatedMultilineMaxLines());

    assertTrue(state.updateTrackedCapability("multiline", false));
    snapshot = state.snapshot();
    assertEquals(0L, snapshot.multilineMaxBytes());
    assertEquals(0L, snapshot.multilineMaxLines());
    assertEquals(2048L, snapshot.negotiatedMultilineMaxBytes());
    assertEquals(3L, snapshot.negotiatedMultilineMaxLines());
  }

  @Test
  void derivesTypingAndMonitorAvailabilityFromPolicyAndIsupport() {
    Ircv3CapabilityState state = new Ircv3CapabilityState();
    state.setMessageTagsCapAcked(true);

    assertTrue(state.snapshot().typingAvailable());
    assertTrue(state.updateTypingClientTagPolicy(false));
    assertFalse(state.snapshot().typingAvailable());
    assertFalse(state.updateTypingClientTagPolicy(false));

    assertTrue(state.updateMonitorSupport(true, -5L));
    assertTrue(state.snapshot().monitorAvailable());
    assertEquals(0L, state.snapshot().monitorMaxTargets());
  }

  @Test
  void gatesFallbackRequestsAndOneShotDiagnostics() {
    Ircv3CapabilityState state = new Ircv3CapabilityState();

    assertTrue(state.beginMessageTagsFallbackRequest());
    assertFalse(state.beginMessageTagsFallbackRequest());
    state.clearMessageTagsFallbackRequest();
    assertTrue(state.beginMessageTagsFallbackRequest());

    assertTrue(state.beginCapabilitySummaryObservation());
    assertFalse(state.beginCapabilitySummaryObservation());
    assertTrue(state.shouldWarnMissingServerTime());
    assertFalse(state.shouldWarnMissingServerTime());
    assertTrue(state.shouldWarnUnavailableTyping());
    assertFalse(state.shouldWarnUnavailableTyping());
  }

  @Test
  void resetClearsConnectionScopedStateButKeepsApplicationRunServerTimeWarning() {
    Ircv3CapabilityState state = new Ircv3CapabilityState();
    state.updateTrackedCapability("server-time", true);
    state.updateTrackedCapability("message-tags", true);
    state.setMultilineOfferedMaxBytes(false, 4096L);
    state.beginBatchFallbackRequest();
    state.beginCapabilitySummaryObservation();
    state.shouldWarnMissingServerTime();
    state.shouldWarnUnavailableTyping();

    state.resetConnectionSession();

    Ircv3CapabilitySnapshot snapshot = state.snapshot();
    assertFalse(snapshot.serverTimeCapAcked());
    assertFalse(snapshot.messageTagsCapAcked());
    assertEquals(0L, state.multilineOfferedMaxBytes(false));
    assertTrue(state.beginBatchFallbackRequest());
    assertTrue(state.beginCapabilitySummaryObservation());
    assertFalse(state.shouldWarnMissingServerTime());
    assertTrue(state.shouldWarnUnavailableTyping());
  }
}
