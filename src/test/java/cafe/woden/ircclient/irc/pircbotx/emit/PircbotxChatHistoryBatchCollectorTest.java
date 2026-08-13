package cafe.woden.ircclient.irc.pircbotx.emit;

import static cafe.woden.ircclient.irc.pircbotx.PircbotxRuntimeTestFixtures.chatHistoryBatches;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.irc.*;
import cafe.woden.ircclient.irc.backend.*;
import cafe.woden.ircclient.irc.ircv3.*;
import cafe.woden.ircclient.irc.ircv3.spi.*;
import cafe.woden.ircclient.irc.playback.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

class PircbotxChatHistoryBatchCollectorTest {

  @Test
  void appendIfActiveBuffersEntriesUntilBatchEnds() {
    List<ServerIrcEvent> events = new ArrayList<>();
    PircbotxChatHistoryBatchCollector collector = chatHistoryBatches("libera", events::add);

    assertTrue(
        collector.handleBatchControlLine(":server.example BATCH +abc draft/chathistory #ircafe"));
    assertTrue(
        collector.appendIfActive(
            "abc",
            ChatHistoryEntry.Kind.PRIVMSG,
            Instant.parse("2026-03-13T12:00:00Z"),
            "#fallback",
            "alice",
            "hello",
            "msg-1",
            Map.of("msgid", "msg-1")));
    assertTrue(collector.handleBatchControlLine(":server.example BATCH -abc"));

    assertEquals(1, events.size());
    IrcEvent.ChatHistoryBatchReceived batch =
        assertInstanceOf(IrcEvent.ChatHistoryBatchReceived.class, events.getFirst().event());
    assertEquals("libera", events.getFirst().serverId());
    assertEquals("#ircafe", batch.target());
    assertEquals("abc", batch.batchId());
    assertEquals(1, batch.entries().size());
    assertEquals(ChatHistoryEntry.Kind.PRIVMSG, batch.entries().getFirst().kind());
    assertEquals("alice", batch.entries().getFirst().from());
    assertEquals("hello", batch.entries().getFirst().text());
  }

  @Test
  void runtimeProvidersOwnBatchControlAndReferenceInterpretation() {
    Ircv3InboundCommandSignalProvider commandProvider =
        new Ircv3InboundCommandSignalProvider() {
          @Override
          public String providerId() {
            return "custom-history";
          }

          @Override
          public Set<Ircv3InboundCommandOperation> inboundCommandOperations() {
            return Set.of(Ircv3InboundCommandOperation.HISTORY_BATCH_CONTROL);
          }

          @Override
          public List<Ircv3InboundCommandSignal> parse(
              Ircv3InboundCommandOperation operation, Ircv3InboundCommandRequest request) {
            if (request.rawLine().contains("end")) {
              return List.of(new Ircv3InboundCommandSignal.HistoryBatchEnded("runtime-1"));
            }
            return List.of(
                new Ircv3InboundCommandSignal.HistoryBatchStarted(
                    "runtime-1", "chathistory", "#runtime"));
          }
        };
    Ircv3InboundTagSignalProvider tagProvider =
        new Ircv3InboundTagSignalProvider() {
          @Override
          public String providerId() {
            return "custom-history";
          }

          @Override
          public Set<Ircv3InboundTagOperation> inboundTagOperations() {
            return Set.of(Ircv3InboundTagOperation.HISTORY_BATCH_REFERENCE);
          }

          @Override
          public List<Ircv3InboundTagSignal> parse(
              Ircv3InboundTagOperation operation, Ircv3InboundTagRequest request) {
            return List.of(
                Ircv3InboundTagSignal.of(
                    Ircv3InboundTagSignalType.HISTORY_BATCH_REFERENCE, "runtime-1"));
          }
        };

    List<ServerIrcEvent> events = new ArrayList<>();
    Ircv3RuntimeTestFixtures.Runtime runtime = Ircv3RuntimeTestFixtures.runtime();
    PircbotxChatHistoryBatchCollector collector =
        new PircbotxChatHistoryBatchCollector(
            "libera",
            events::add,
            Ircv3InboundCommandSignalRuntimeCatalog.fromProviders(List.of(commandProvider)),
            Ircv3InboundTagSignalRuntimeCatalog.fromProviders(List.of(tagProvider)),
            runtime.serverTime(),
            runtime.messageTags());

    assertEquals(Optional.of("runtime-1"), collector.batchId(Map.of("batch", "ignored")));
    assertTrue(collector.handleBatchControlLine("custom start"));
    assertTrue(
        collector.appendIfActive(
            "runtime-1",
            ChatHistoryEntry.Kind.NOTICE,
            Instant.EPOCH,
            "#fallback",
            "server",
            "history",
            "msg-runtime",
            Map.of()));
    assertTrue(collector.handleBatchControlLine("custom end"));

    IrcEvent.ChatHistoryBatchReceived batch =
        assertInstanceOf(IrcEvent.ChatHistoryBatchReceived.class, events.getFirst().event());
    assertEquals("#runtime", batch.target());
    assertEquals("runtime-1", batch.batchId());
    assertEquals("history", batch.entries().getFirst().text());
  }

  @Test
  void maybeCaptureUnknownLineUsesBatchTagAndBuffersPrivmsgEntries() {
    List<ServerIrcEvent> events = new ArrayList<>();
    PircbotxChatHistoryBatchCollector collector = chatHistoryBatches("libera", events::add);

    assertTrue(collector.handleBatchControlLine(":server.example BATCH +hist chathistory #ircafe"));
    assertTrue(
        collector.maybeCaptureUnknownLine(
            "@batch=hist;msgid=znc-1;time=2026-03-13T12:01:00Z "
                + ":alice!u@example PRIVMSG #ircafe :waves",
            ":alice!u@example PRIVMSG #ircafe :waves"));
    assertTrue(collector.handleBatchControlLine(":server.example BATCH -hist"));

    IrcEvent.ChatHistoryBatchReceived batch =
        assertInstanceOf(IrcEvent.ChatHistoryBatchReceived.class, events.getFirst().event());
    assertEquals(1, batch.entries().size());
    ChatHistoryEntry entry = batch.entries().getFirst();
    assertEquals(ChatHistoryEntry.Kind.PRIVMSG, entry.kind());
    assertEquals("#ircafe", entry.target());
    assertEquals("alice", entry.from());
    assertEquals("waves", entry.text());
    assertEquals("znc-1", entry.messageId());
  }
}
