package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignal;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignalProvider;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class Ircv3CapabilityNegotiationRuntimeSupportTest {

  @Test
  void reconstructsBuiltInCapabilityChangesAndFallbackRequests() {
    Ircv3CapabilityNegotiationRuntimeSupport support =
        new Ircv3CapabilityNegotiationRuntimeSupport(
            Ircv3InboundCommandSignalRuntimeCatalog.applicationClasspath());

    Ircv3CapabilityNegotiationRuntimeSupport.Plan plan =
        support.plan(
            request(
                "LS",
                "message-tags batch draft/chathistory",
                false,
                false,
                false,
                Set.of()));

    assertEquals(3, plan.changes().size());
    assertFalse(plan.refreshConnectionFeatures());
    assertTrue(plan.requestMessageTags());
    assertTrue(plan.requestBatch());
    assertEquals("draft/chathistory", plan.historyCapability());
  }

  @Test
  void validatesPluginSignalsAgainstTheObservedCapAction() {
    Ircv3InboundCommandSignalProvider provider =
        new Ircv3InboundCommandSignalProvider() {
          @Override
          public String providerId() {
            return "override";
          }

          @Override
          public Set<Ircv3InboundCommandOperation> inboundCommandOperations() {
            return Set.of(Ircv3InboundCommandOperation.CAP_NEGOTIATION);
          }

          @Override
          public List<Ircv3InboundCommandSignal> parse(
              Ircv3InboundCommandOperation operation, Ircv3InboundCommandRequest request) {
            return List.of(
                new Ircv3InboundCommandSignal.CapabilityChangeObserved(
                    "ACK", "message-tags", true, true),
                new Ircv3InboundCommandSignal.CapabilityChangeObserved(
                    "LS", "bad name", true, true),
                new Ircv3InboundCommandSignal.CapabilityFallbackPlanned(
                    true, true, "untrusted/history"));
          }
        };
    Ircv3CapabilityNegotiationRuntimeSupport support =
        new Ircv3CapabilityNegotiationRuntimeSupport(
            Ircv3InboundCommandSignalRuntimeCatalog.fromProviders(List.of(provider)));

    Ircv3CapabilityNegotiationRuntimeSupport.Plan plan =
        support.plan(request("ACK", "message-tags", false, false, false, Set.of()));

    assertEquals(
        List.of(
            new Ircv3CapabilityNegotiationRuntimeSupport.CapabilityChange(
                "ACK", "message-tags", true, true)),
        plan.changes());
    assertTrue(plan.refreshConnectionFeatures());
    assertFalse(plan.requestMessageTags());
    assertFalse(plan.requestBatch());
    assertEquals("", plan.historyCapability());
  }

  private static Ircv3InboundCommandRequest request(
      String action,
      String capabilities,
      boolean messageTagsEnabled,
      boolean batchEnabled,
      boolean chatHistoryEnabled,
      Set<String> pendingCapabilities) {
    return new Ircv3InboundCommandRequest(
        "server",
        "CAP",
        "",
        List.of("*", action, capabilities),
        Map.of(),
        "",
        false,
        0L,
        messageTagsEnabled,
        batchEnabled,
        chatHistoryEnabled,
        pendingCapabilities);
  }
}
