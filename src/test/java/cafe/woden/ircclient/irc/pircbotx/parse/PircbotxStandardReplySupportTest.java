package cafe.woden.ircclient.irc.pircbotx.parse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.irc.IrcEvent;
import cafe.woden.ircclient.irc.ServerIrcEvent;
import cafe.woden.ircclient.irc.ircv3.Ircv3InboundCommandSignalRuntimeCatalog;
import cafe.woden.ircclient.irc.ircv3.Ircv3StandardReplyRuntimeSupport;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignal;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignalProvider;
import com.google.common.collect.ImmutableMap;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class PircbotxStandardReplySupportTest {

  @Test
  void emitsStructuredStandardReplyEvent() {
    List<ServerIrcEvent> out = new ArrayList<>();
    PircbotxStandardReplySupport support =
        PircbotxParserRuntimeTestFixtures.standardReplies("libera", out::add);

    boolean handled =
        support.emitIfSupported(
            Instant.parse("2026-03-22T12:15:00Z"),
            "FAIL",
            "@label=req-42;msgid=srv-1 :server FAIL CHATHISTORY INVALID_PARAMS timestamp=bad :Invalid selector",
            List.of("CHATHISTORY", "INVALID_PARAMS", "timestamp=bad", ":Invalid selector"),
            ImmutableMap.of("label", "req-42", "msgid", "srv-1"));

    assertTrue(handled);
    IrcEvent.StandardReply reply =
        out.stream()
            .map(ServerIrcEvent::event)
            .filter(IrcEvent.StandardReply.class::isInstance)
            .map(IrcEvent.StandardReply.class::cast)
            .findFirst()
            .orElseThrow();
    assertEquals(IrcEvent.StandardReplyKind.FAIL, reply.kind());
    assertEquals("CHATHISTORY", reply.command());
    assertEquals("INVALID_PARAMS", reply.code());
    assertEquals("timestamp=bad", reply.context());
    assertEquals("Invalid selector", reply.description());
    assertEquals("srv-1", reply.messageId());
    assertEquals("req-42", reply.ircv3Tags().get("label"));
  }

  @Test
  void usesRuntimeProviderOverride() {
    List<ServerIrcEvent> out = new ArrayList<>();
    PircbotxStandardReplySupport support =
        new PircbotxStandardReplySupport(
            "libera",
            out::add,
            new Ircv3StandardReplyRuntimeSupport(
                Ircv3InboundCommandSignalRuntimeCatalog.fromProviders(
                    List.of(new OverrideProvider())),
                PircbotxParserRuntimeTestFixtures.runtime().messageId()));

    boolean handled =
        support.emitIfSupported(
            Instant.parse("2026-03-22T12:16:00Z"),
            "NOTE",
            ":server NOTE AUTHENTICATE COMPLETE :done",
            List.of("AUTHENTICATE", "COMPLETE", ":done"),
            ImmutableMap.of("msgid", "override-1"));

    assertTrue(handled);
    IrcEvent.StandardReply reply = (IrcEvent.StandardReply) out.getFirst().event();
    assertEquals("PLUGIN", reply.command());
    assertEquals("CUSTOM", reply.code());
    assertEquals("override-1", reply.messageId());
  }

  @Test
  void ignoresNonStandardReplyCommands() {
    List<ServerIrcEvent> out = new ArrayList<>();
    PircbotxStandardReplySupport support =
        PircbotxParserRuntimeTestFixtures.standardReplies("libera", out::add);

    boolean handled =
        support.emitIfSupported(
            Instant.parse("2026-03-22T12:20:00Z"),
            "PRIVMSG",
            ":bob!u@h PRIVMSG #ircafe :hello",
            List.of("#ircafe", ":hello"),
            ImmutableMap.of());

    assertFalse(handled);
    assertTrue(out.isEmpty());
  }

  private static final class OverrideProvider implements Ircv3InboundCommandSignalProvider {

    @Override
    public String providerId() {
      return "test-standard-reply-override";
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
              Ircv3InboundCommandSignal.StandardReplyKind.NOTE,
              "PLUGIN",
              "CUSTOM",
              "",
              "custom description"));
    }
  }
}
