package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignal;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignalProvider;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class Ircv3StandardReplyRuntimeSupportTest {

  @Test
  void validatesProviderObservationAndResolvesMessageIdAliases() {
    Ircv3StandardReplyRuntimeSupport support = support(new MatchingProvider());

    Ircv3StandardReplyRuntimeSupport.Observation observed =
        support
            .observe(
                "FAIL",
                ":server FAIL CHATHISTORY INVALID_PARAMS timestamp=bad :Invalid selector",
                List.of("CHATHISTORY", "INVALID_PARAMS", "timestamp=bad", ":Invalid selector"),
                Map.of("+draft/msgid", "reply-42"),
                "fallback")
            .orElseThrow();

    assertEquals(Ircv3StandardReplyRuntimeSupport.Kind.FAIL, observed.kind());
    assertEquals("CHATHISTORY", observed.command());
    assertEquals("INVALID_PARAMS", observed.code());
    assertEquals("timestamp=bad", observed.context());
    assertEquals("Invalid selector", observed.description());
    assertEquals("reply-42", observed.messageId());
  }

  @Test
  void rejectsMismatchedOrAmbiguousProviderOutput() {
    Ircv3StandardReplyRuntimeSupport mismatched = support(new MismatchedProvider());
    Ircv3StandardReplyRuntimeSupport ambiguous = support(new AmbiguousProvider());

    assertTrue(mismatched.observe("FAIL", "raw", List.of("CMD", "CODE"), Map.of(), "").isEmpty());
    assertTrue(ambiguous.observe("NOTE", "raw", List.of("CMD", "CODE"), Map.of(), "").isEmpty());
  }

  private static Ircv3StandardReplyRuntimeSupport support(
      Ircv3InboundCommandSignalProvider provider) {
    return new Ircv3StandardReplyRuntimeSupport(
        Ircv3InboundCommandSignalRuntimeCatalog.fromProviders(List.of(provider)),
        Ircv3RuntimeTestFixtures.messageId());
  }

  private static class MatchingProvider implements Ircv3InboundCommandSignalProvider {

    @Override
    public String providerId() {
      return "test-standard-replies";
    }

    @Override
    public Set<Ircv3InboundCommandOperation> inboundCommandOperations() {
      return Set.of(Ircv3InboundCommandOperation.STANDARD_REPLY);
    }

    @Override
    public List<Ircv3InboundCommandSignal> parse(
        Ircv3InboundCommandOperation operation, Ircv3InboundCommandRequest request) {
      return List.of(
          new Ircv3InboundCommandSignal.StandardReplyObserved(
              Ircv3InboundCommandSignal.StandardReplyKind.FAIL,
              "CHATHISTORY",
              "INVALID_PARAMS",
              "timestamp=bad",
              "Invalid selector"));
    }
  }

  private static final class MismatchedProvider extends MatchingProvider {
    @Override
    public List<Ircv3InboundCommandSignal> parse(
        Ircv3InboundCommandOperation operation, Ircv3InboundCommandRequest request) {
      return List.of(
          new Ircv3InboundCommandSignal.StandardReplyObserved(
              Ircv3InboundCommandSignal.StandardReplyKind.NOTE, "CHATHISTORY", "OK", "", "done"));
    }
  }

  private static final class AmbiguousProvider extends MatchingProvider {
    @Override
    public List<Ircv3InboundCommandSignal> parse(
        Ircv3InboundCommandOperation operation, Ircv3InboundCommandRequest request) {
      Ircv3InboundCommandSignal reply =
          new Ircv3InboundCommandSignal.StandardReplyObserved(
              Ircv3InboundCommandSignal.StandardReplyKind.NOTE, "CHATHISTORY", "OK", "", "done");
      return List.of(reply, reply);
    }
  }
}
