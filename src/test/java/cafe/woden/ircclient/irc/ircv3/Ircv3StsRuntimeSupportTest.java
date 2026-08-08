package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignal;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignalProvider;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class Ircv3StsRuntimeSupportTest {

  @Test
  void acceptsValidPluginPolicyObservation() {
    Ircv3StsRuntimeSupport support = support(validProvider());

    var decision =
        support
            .observe(
                "IRC.Example.NET",
                true,
                "sts=duration=60,port=6697,preload",
                1_000L)
            .getFirst();

    assertEquals(Ircv3StsPolicyLearningPlanner.Outcome.LEARN, decision.outcome());
    Ircv3StsPolicy policy = decision.policy().orElseThrow();
    assertEquals("irc.example.net", policy.hostLower());
    assertEquals(61_000L, policy.expiresAtEpochMs());
    assertEquals(Integer.valueOf(6697), policy.port());
    assertTrue(policy.preload());
  }

  @Test
  void rejectsProviderPolicyForDifferentHost() {
    Ircv3InboundCommandSignalProvider provider =
        provider(
            new Ircv3InboundCommandSignal.StsPolicyObserved(
                Ircv3InboundCommandSignal.StsPolicyOutcome.LEARN,
                "attacker.example.net",
                "duration=60",
                61_000L,
                null,
                false,
                60L));

    assertTrue(
        support(provider)
            .observe("irc.example.net", true, "sts=duration=60", 1_000L)
            .isEmpty());
  }

  @Test
  void rejectsExpiredOrMalformedLearnObservation() {
    Ircv3InboundCommandSignalProvider provider =
        provider(
            new Ircv3InboundCommandSignal.StsPolicyObserved(
                Ircv3InboundCommandSignal.StsPolicyOutcome.LEARN,
                "irc.example.net",
                "duration=60",
                1_000L,
                70_000,
                false,
                0L));

    assertTrue(
        support(provider)
            .observe("irc.example.net", true, "sts=duration=60", 1_000L)
            .isEmpty());
  }

  private static Ircv3StsRuntimeSupport support(Ircv3InboundCommandSignalProvider provider) {
    return new Ircv3StsRuntimeSupport(
        Ircv3InboundCommandSignalRuntimeCatalog.fromProviders(List.of(provider)));
  }

  private static Ircv3InboundCommandSignalProvider validProvider() {
    return provider(
        new Ircv3InboundCommandSignal.StsPolicyObserved(
            Ircv3InboundCommandSignal.StsPolicyOutcome.LEARN,
            "irc.example.net",
            "duration=60,port=6697,preload",
            61_000L,
            6697,
            true,
            60L));
  }

  private static Ircv3InboundCommandSignalProvider provider(
      Ircv3InboundCommandSignal signal) {
    return new Ircv3InboundCommandSignalProvider() {
      @Override
      public String providerId() {
        return "test-sts";
      }

      @Override
      public Set<Ircv3InboundCommandOperation> inboundCommandOperations() {
        return Set.of(Ircv3InboundCommandOperation.STS_CAPABILITY);
      }

      @Override
      public List<Ircv3InboundCommandSignal> parse(
          Ircv3InboundCommandOperation operation, Ircv3InboundCommandRequest request) {
        return List.of(signal);
      }
    };
  }
}
