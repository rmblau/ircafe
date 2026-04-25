package cafe.woden.ircclient.ui.chat.transcript.flow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.transcript.filter.ChatTranscriptFilterRoutingSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptPlainAppendSupport;
import cafe.woden.ircclient.ui.chat.transcript.spoiler.ChatTranscriptSpoilerAppendSupport;
import cafe.woden.ircclient.ui.chat.transcript.spoiler.ChatTranscriptSpoilerComponentSupport;
import cafe.woden.ircclient.ui.chat.transcript.spoiler.ChatTranscriptSpoilerHistoryInsertSupport;
import cafe.woden.ircclient.ui.chat.transcript.spoiler.ChatTranscriptSpoilerRevealSupport;
import cafe.woden.ircclient.ui.chat.transcript.spoiler.ChatTranscriptSpoilerRuntimeSupport;
import cafe.woden.ircclient.ui.chat.transcript.spoiler.ChatTranscriptSpoilerWriteSupport;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyledDocument;
import org.junit.jupiter.api.Test;

class ChatTranscriptPlainSpoilerCoordinatorTest {

  @Test
  void appendPlainWritesTextThroughHeldContext() throws Exception {
    TargetRef ref = new TargetRef("srv", "#chan");
    DefaultStyledDocument doc = new DefaultStyledDocument();
    Map<TargetRef, StyledDocument> docs = new HashMap<>();
    AtomicInteger lineCapCalls = new AtomicInteger();
    ChatTranscriptPlainSpoilerCoordinator coordinator =
        new ChatTranscriptPlainSpoilerCoordinator(
            new ChatTranscriptPlainAppendSupport.Context(
                docs,
                new ChatStyles(null),
                target -> docs.put(target, doc),
                target -> {},
                (target, targetDoc) -> {
                  lineCapCalls.incrementAndGet();
                  return 0;
                }),
            spoilerContext());

    coordinator.appendPlain(ref, "plain text");

    assertEquals("plain text", doc.getText(0, doc.getLength()));
    assertEquals(1, lineCapCalls.get());
  }

  @Test
  void insertSpoilerFromHistoryWritesVisibleSpoilerThroughHeldContext() throws Exception {
    TargetRef ref = new TargetRef("srv", "#chan");
    DefaultStyledDocument doc = new DefaultStyledDocument();
    Map<TargetRef, StyledDocument> docs = new HashMap<>();
    docs.put(ref, doc);
    AtomicInteger filteredRunEnds = new AtomicInteger();
    ChatTranscriptPlainSpoilerCoordinator coordinator =
        new ChatTranscriptPlainSpoilerCoordinator(
            new ChatTranscriptPlainAppendSupport.Context(
                docs,
                new ChatStyles(null),
                target -> docs.put(target, doc),
                target -> {},
                (target, targetDoc) -> 0),
            spoilerContext(docs, filteredRunEnds));

    int nextInsertAt =
        coordinator.insertSpoilerChatFromHistoryAt(ref, 0, "alice", "secret", 1_000L);

    assertTrue(nextInsertAt > 0);
    assertEquals(" \n", doc.getText(0, doc.getLength()));
    assertEquals(1, filteredRunEnds.get());
  }

  private static ChatTranscriptSpoilerFlowSupport.Context spoilerContext() {
    return spoilerContext(new HashMap<>(), new AtomicInteger());
  }

  private static ChatTranscriptSpoilerFlowSupport.Context spoilerContext(
      Map<TargetRef, StyledDocument> docs, AtomicInteger filteredRunEnds) {
    return new ChatTranscriptSpoilerFlowSupport.Context(
        docs,
        target -> docs.computeIfAbsent(target, ignored -> new DefaultStyledDocument()),
        (target, epochMs) -> {},
        new ChatTranscriptFilterRoutingSupport(
            null,
            (target, previewText, hiddenMeta, match) -> {},
            (target, insertAt, previewText, hiddenMeta, match) -> insertAt,
            target -> filteredRunEnds.incrementAndGet(),
            target -> {}),
        new ChatTranscriptSpoilerRuntimeSupport.Context(
            null,
            () -> true,
            new ChatTranscriptSpoilerRevealSupport.Context(
                new ChatStyles(null), null, null, (target, fromNick) -> fromNick),
            new Object()),
        new ChatTranscriptSpoilerAppendSupport.Context(
            new ChatStyles(null), spoilerWriteContext(), transcript -> {}, (target, doc) -> 0),
        new ChatTranscriptSpoilerHistoryInsertSupport.Context(
            spoilerWriteContext(),
            (doc, insertAt) -> insertAt,
            (doc, insertAt) -> insertAt,
            (target, insertAt, delta) -> {},
            (target, doc) -> 0),
        target -> filteredRunEnds.incrementAndGet());
  }

  private static ChatTranscriptSpoilerWriteSupport.Context spoilerWriteContext() {
    ChatStyles styles = new ChatStyles(null);
    return new ChatTranscriptSpoilerWriteSupport.Context(
        styles,
        new ChatTranscriptSpoilerComponentSupport.Context(
            null, null, (target, fromNick) -> fromNick),
        (base, filterMatch) -> new javax.swing.text.SimpleAttributeSet(base));
  }
}
