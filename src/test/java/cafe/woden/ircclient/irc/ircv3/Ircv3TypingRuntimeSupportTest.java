package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignal;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignalProvider;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagSignal;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagSignalProvider;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagSignalType;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3OutboundCommandOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3OutboundCommandProvider;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3OutboundCommandRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class Ircv3TypingRuntimeSupportTest {

  @Test
  void rendersCanonicalTypingCommandThroughBuiltInProvider() {
    Ircv3TypingRuntimeSupport support = Ircv3RuntimeTestFixtures.runtime().typing();

    assertEquals(
        new Ircv3TypingRuntimeSupport.OutboundPlan(
            "@+typing=active TAGMSG #ircafe", "#ircafe", "active"),
        support.render("#ircafe", "composing").orElseThrow());
    assertTrue(support.outboundAvailable());
    assertTrue(support.render("#ircafe", "unknown").isEmpty());
  }

  @Test
  void acceptsValidReplacementProviderOutput() {
    Ircv3TypingRuntimeSupport support =
        support(new OutboundProvider("@+typing=paused TAGMSG #ircafe"), null, null);

    assertEquals(
        new Ircv3TypingRuntimeSupport.OutboundPlan(
            "@+typing=paused TAGMSG #ircafe", "#ircafe", "paused"),
        support.render("#ircafe", "composing").orElseThrow());
  }

  @Test
  void rejectsUnsafeOrTargetChangingReplacementOutput() {
    Ircv3TypingRuntimeSupport changedTarget =
        support(new OutboundProvider("@+typing=active TAGMSG #elsewhere"), null, null);
    Ircv3TypingRuntimeSupport unsafe =
        support(new OutboundProvider("@+typing=active TAGMSG #ircafe\r\nOPER root"), null, null);

    assertThrows(IllegalStateException.class, () -> changedTarget.render("#ircafe", "active"));
    assertThrows(IllegalStateException.class, () -> unsafe.render("#ircafe", "active"));
  }

  @Test
  void canonicalizesBuiltInInboundTypingAliases() {
    Ircv3TypingRuntimeSupport support = Ircv3RuntimeTestFixtures.runtime().typing();
    Ircv3InboundTagRequest request =
        new Ircv3InboundTagRequest(
            "TAGMSG", "alice", "#ircafe", List.of("#ircafe"), Map.of("typing", "composing"));

    assertEquals(
        new Ircv3TypingRuntimeSupport.TagObservation("active"),
        support.fromTags(request).orElseThrow());
  }

  @Test
  void rejectsAmbiguousOrNoncanonicalInboundProviderOutput() {
    Ircv3InboundTagRequest request =
        new Ircv3InboundTagRequest("TAGMSG", "alice", "#ircafe", List.of("#ircafe"), Map.of());
    Ircv3TypingRuntimeSupport ambiguous =
        support(
            null,
            new TagProvider(
                List.of(
                    Ircv3InboundTagSignal.of(Ircv3InboundTagSignalType.TYPING, "active"),
                    Ircv3InboundTagSignal.of(Ircv3InboundTagSignalType.TYPING, "paused"))),
            null);
    Ircv3TypingRuntimeSupport noncanonical =
        support(
            null,
            new TagProvider(
                List.of(Ircv3InboundTagSignal.of(Ircv3InboundTagSignalType.TYPING, "composing"))),
            null);

    assertTrue(ambiguous.fromTags(request).isEmpty());
    assertTrue(noncanonical.fromTags(request).isEmpty());
  }

  @Test
  void interpretsTypingClientTagPolicyThroughBuiltInProvider() {
    Ircv3TypingRuntimeSupport support = Ircv3RuntimeTestFixtures.runtime().typing();

    assertEquals(
        new Ircv3TypingRuntimeSupport.ClientTagPolicy(true, "*,-typing"),
        support
            .clientTagPolicy(":server 005 me CLIENTTAGDENY=*,-typing :are supported")
            .orElseThrow());
    assertEquals(
        new Ircv3TypingRuntimeSupport.ClientTagPolicy(false, "typing,react"),
        support
            .clientTagPolicy(":server 005 me CLIENTTAGDENY=typing,react :are supported")
            .orElseThrow());
  }

  @Test
  void rejectsAmbiguousTypingClientTagPolicyOutput() {
    Ircv3TypingRuntimeSupport support =
        support(
            null,
            null,
            new CommandProvider(
                List.of(
                    new Ircv3InboundCommandSignal.ClientTagPolicyObserved(
                        "typing", true, "*,-typing"),
                    new Ircv3InboundCommandSignal.ClientTagPolicyObserved(
                        "typing", false, "typing"))));

    assertFalse(
        support
            .clientTagPolicy(":server 005 me CLIENTTAGDENY=*,-typing :are supported")
            .isPresent());
  }

  private static Ircv3TypingRuntimeSupport support(
      Ircv3OutboundCommandProvider outboundProvider,
      Ircv3InboundTagSignalProvider tagProvider,
      Ircv3InboundCommandSignalProvider commandProvider) {
    return new Ircv3TypingRuntimeSupport(
        Ircv3OutboundCommandRuntimeCatalog.fromProviders(
            outboundProvider == null ? List.of() : List.of(outboundProvider)),
        Ircv3InboundTagSignalRuntimeCatalog.fromProviders(
            tagProvider == null ? List.of() : List.of(tagProvider)),
        Ircv3InboundCommandSignalRuntimeCatalog.fromProviders(
            commandProvider == null ? List.of() : List.of(commandProvider)));
  }

  private record OutboundProvider(String rawLine) implements Ircv3OutboundCommandProvider {

    @Override
    public String providerId() {
      return "typing-test-outbound";
    }

    @Override
    public Set<Ircv3OutboundCommandOperation> operations() {
      return Set.of(Ircv3OutboundCommandOperation.TYPING);
    }

    @Override
    public List<String> build(
        Ircv3OutboundCommandOperation operation, Ircv3OutboundCommandRequest request) {
      return List.of(rawLine);
    }
  }

  private record TagProvider(List<Ircv3InboundTagSignal> signals)
      implements Ircv3InboundTagSignalProvider {

    @Override
    public String providerId() {
      return "typing-test-tags";
    }

    @Override
    public Set<Ircv3InboundTagOperation> inboundTagOperations() {
      return Set.of(Ircv3InboundTagOperation.TYPING);
    }

    @Override
    public List<Ircv3InboundTagSignal> parse(
        Ircv3InboundTagOperation operation, Ircv3InboundTagRequest request) {
      return signals;
    }
  }

  private record CommandProvider(List<Ircv3InboundCommandSignal> signals)
      implements Ircv3InboundCommandSignalProvider {

    @Override
    public String providerId() {
      return "typing-test-command";
    }

    @Override
    public Set<Ircv3InboundCommandOperation> inboundCommandOperations() {
      return Set.of(Ircv3InboundCommandOperation.ISUPPORT_CLIENT_TAG_POLICY);
    }

    @Override
    public List<Ircv3InboundCommandSignal> parse(
        Ircv3InboundCommandOperation operation, Ircv3InboundCommandRequest request) {
      return signals;
    }
  }
}
