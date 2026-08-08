package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;

import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagSignal;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagSignalProvider;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagSignalType;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class Ircv3MessageIdRuntimeSupportTest {

  @Test
  void resolvesBuiltInAliasesInStableDraftAndBackendOrder() {
    Ircv3MessageIdRuntimeSupport support = Ircv3RuntimeTestFixtures.messageId();

    assertEquals(
        "stable",
        support.resolve(
            Map.of("znc.in/msgid", "backend", "draft/msgid", "draft", "msgid", "stable")));
    assertEquals("draft", support.resolve(Map.of("+draft/msgid", "draft")));
    assertEquals("backend", support.resolve(Map.of("+znc.in/msgid", "backend")));
  }

  @Test
  void installedProviderCanReplaceMessageIdInterpretation() {
    Ircv3MessageIdRuntimeSupport support =
        new Ircv3MessageIdRuntimeSupport(
            Ircv3InboundTagSignalRuntimeCatalog.fromProviders(
                List.of(
                    provider(
                        100,
                        List.of(
                            Ircv3InboundTagSignal.of(
                                Ircv3InboundTagSignalType.MESSAGE_ID, "custom"))))));

    assertEquals("custom", support.resolve(Map.of("vendor/id", "ignored")));
  }

  @Test
  void rejectsAmbiguousOrUnsafeProviderOutputAndValidatesFallback() {
    Ircv3MessageIdRuntimeSupport ambiguous =
        new Ircv3MessageIdRuntimeSupport(
            Ircv3InboundTagSignalRuntimeCatalog.fromProviders(
                List.of(
                    provider(
                        100,
                        List.of(
                            Ircv3InboundTagSignal.of(
                                Ircv3InboundTagSignalType.MESSAGE_ID, "one"),
                            Ircv3InboundTagSignal.of(
                                Ircv3InboundTagSignalType.MESSAGE_ID, "two"))))));
    Ircv3MessageIdRuntimeSupport unsafe =
        new Ircv3MessageIdRuntimeSupport(
            Ircv3InboundTagSignalRuntimeCatalog.fromProviders(
                List.of(
                    provider(
                        100,
                        List.of(
                            Ircv3InboundTagSignal.of(
                                Ircv3InboundTagSignalType.MESSAGE_ID, "bad id"))))));
    Ircv3MessageIdRuntimeSupport empty =
        new Ircv3MessageIdRuntimeSupport(
            Ircv3InboundTagSignalRuntimeCatalog.fromProviders(List.of(provider(100, List.of()))));

    assertEquals("", ambiguous.resolve(Map.of(), "fallback"));
    assertEquals("", unsafe.resolve(Map.of(), "fallback"));
    assertEquals("fallback", empty.resolve(Map.of(), "fallback"));
    assertEquals("", empty.resolve(Map.of(), "bad fallback"));
  }

  private static Ircv3InboundTagSignalProvider provider(
      int priority, List<Ircv3InboundTagSignal> signals) {
    return new Ircv3InboundTagSignalProvider() {
      @Override
      public String providerId() {
        return "message-id-test";
      }

      @Override
      public int inboundTagPriority() {
        return priority;
      }

      @Override
      public Set<Ircv3InboundTagOperation> inboundTagOperations() {
        return Set.of(Ircv3InboundTagOperation.MESSAGE_ID);
      }

      @Override
      public List<Ircv3InboundTagSignal> parse(
          Ircv3InboundTagOperation operation, Ircv3InboundTagRequest request) {
        return signals;
      }
    };
  }
}
