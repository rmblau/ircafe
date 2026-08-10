package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagSignal;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagSignalProvider;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagSignalType;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class Ircv3LabeledResponseRuntimeSupportTest {

  @Test
  void adaptsBuiltInTagAndRawLineSignals() {
    Ircv3LabeledResponseRuntimeSupport support = Ircv3RuntimeTestFixtures.labeledResponse();

    Ircv3LabeledResponseRuntimeSupport.Observation success =
        support.fromTags("NOTE", Map.of("label", "request-1")).orElseThrow();
    Ircv3LabeledResponseRuntimeSupport.Observation failure =
        support.fromRawLine("FAIL", "@label=request-2 :server FAIL TEST BAD :no").orElseThrow();

    assertEquals("request-1", success.label());
    assertEquals(Ircv3LabeledResponseRuntimeSupport.Outcome.SUCCESS, success.outcome());
    assertEquals("request-2", failure.label());
    assertEquals(Ircv3LabeledResponseRuntimeSupport.Outcome.FAILURE, failure.outcome());
  }

  @Test
  void runtimeProviderCanReplaceLabeledResponseInterpretation() {
    Ircv3InboundTagSignalProvider provider =
        new Ircv3InboundTagSignalProvider() {
          @Override
          public String providerId() {
            return "custom-labels";
          }

          @Override
          public Set<Ircv3InboundTagOperation> inboundTagOperations() {
            return Set.of(Ircv3InboundTagOperation.LABELED_RESPONSE);
          }

          @Override
          public List<Ircv3InboundTagSignal> parse(
              Ircv3InboundTagOperation operation, Ircv3InboundTagRequest request) {
            return List.of(
                new Ircv3InboundTagSignal(
                    Ircv3InboundTagSignalType.LABELED_RESPONSE, "plugin-label", "FAILURE"));
          }
        };
    Ircv3LabeledResponseRuntimeSupport support =
        new Ircv3LabeledResponseRuntimeSupport(
            Ircv3InboundTagSignalRuntimeCatalog.fromProviders(List.of(provider)));

    Ircv3LabeledResponseRuntimeSupport.Observation observed =
        support.fromTags("NOTE", Map.of()).orElseThrow();

    assertEquals("plugin-label", observed.label());
    assertEquals(Ircv3LabeledResponseRuntimeSupport.Outcome.FAILURE, observed.outcome());
    assertTrue(support.fromRawLine("", "").isPresent());
  }
}
