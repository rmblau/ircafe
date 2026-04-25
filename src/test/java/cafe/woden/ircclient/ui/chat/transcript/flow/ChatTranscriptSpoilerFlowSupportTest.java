package cafe.woden.ircclient.ui.chat.transcript.flow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cafe.woden.ircclient.model.FilterAction;
import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.transcript.filter.ChatTranscriptFilterRoutingSupport;
import cafe.woden.ircclient.ui.chat.transcript.spoiler.ChatTranscriptSpoilerAppendSupport;
import cafe.woden.ircclient.ui.chat.transcript.spoiler.ChatTranscriptSpoilerComponentSupport;
import cafe.woden.ircclient.ui.chat.transcript.spoiler.ChatTranscriptSpoilerHistoryInsertSupport;
import cafe.woden.ircclient.ui.chat.transcript.spoiler.ChatTranscriptSpoilerRevealSupport;
import cafe.woden.ircclient.ui.chat.transcript.spoiler.ChatTranscriptSpoilerRuntimeSupport;
import cafe.woden.ircclient.ui.chat.transcript.spoiler.ChatTranscriptSpoilerWriteSupport;
import cafe.woden.ircclient.ui.filter.FilterEngine;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.ObjLongConsumer;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyledDocument;
import org.junit.jupiter.api.Test;

class ChatTranscriptSpoilerFlowSupportTest {

  @Test
  void appendSpoilerWritesVisibleSpoilerLineWhenUnfiltered() throws Exception {
    AtomicInteger lineCapCalls = new AtomicInteger();
    ChatTranscriptSpoilerFlowSupport.Context context =
        newContext(
            new ChatTranscriptFilterRoutingSupport(
                null,
                (ref, previewText, hiddenMeta, match) -> {},
                (ref, insertAt, previewText, hiddenMeta, match) -> insertAt,
                ref -> {},
                ref -> {}),
            new ChatTranscriptSpoilerAppendSupport.Context(
                new ChatStyles(null),
                spoilerWriteContext(),
                doc -> {},
                (ref, doc) -> {
                  lineCapCalls.incrementAndGet();
                  return 0;
                }),
            historyInsertContext(),
            ref -> {});
    DefaultStyledDocument doc = new DefaultStyledDocument();

    ChatTranscriptSpoilerFlowSupport.appendSpoiler(
        context, doc, new TargetRef("srv", "#chan"), "alice", "secret", 1_000L);

    assertEquals(" \n", doc.getText(0, doc.getLength()));
    assertEquals(1, lineCapCalls.get());
  }

  @Test
  void appendSpoilerEnsuresTargetAndNotesHistoryEpochBeforeWriting() throws Exception {
    TargetRef ref = new TargetRef("srv", "#chan");
    Map<TargetRef, StyledDocument> docs = new HashMap<>();
    DefaultStyledDocument doc = new DefaultStyledDocument();
    AtomicInteger targetEnsures = new AtomicInteger();
    AtomicLong notedEpoch = new AtomicLong(-1L);
    ChatTranscriptSpoilerFlowSupport.Context context =
        newContext(
            docs,
            target -> {
              assertEquals(ref, target);
              targetEnsures.incrementAndGet();
              docs.put(target, doc);
            },
            (target, epochMs) -> {
              assertEquals(ref, target);
              notedEpoch.set(epochMs);
            },
            new ChatTranscriptFilterRoutingSupport(
                null,
                (target, previewText, hiddenMeta, match) -> {},
                (target, insertAt, previewText, hiddenMeta, match) -> insertAt,
                target -> {},
                target -> {}),
            appendContext(),
            historyInsertContext(),
            target -> {});

    ChatTranscriptSpoilerFlowSupport.appendSpoiler(context, ref, "alice", "secret", 1_000L);

    assertEquals(" \n", doc.getText(0, doc.getLength()));
    assertEquals(1, targetEnsures.get());
    assertEquals(1_000L, notedEpoch.get());
  }

  @Test
  void insertSpoilerFromHistoryReturnsHiddenInsertOffsetWhenHidePlaceholderHandled() {
    FilterEngine filterEngine = mock(FilterEngine.class);
    when(filterEngine.firstMatch(any()))
        .thenReturn(new FilterEngine.Match(UUID.randomUUID(), "hide", FilterAction.HIDE));
    when(filterEngine.effectiveFor(any()))
        .thenReturn(new FilterEngine.Effective(true, true, true, 3, 250, 12, 10, true));

    AtomicInteger filteredRunEnds = new AtomicInteger();
    ChatTranscriptSpoilerFlowSupport.Context context =
        newContext(
            new ChatTranscriptFilterRoutingSupport(
                filterEngine,
                (ref, previewText, hiddenMeta, match) -> {},
                (ref, insertAt, previewText, hiddenMeta, match) -> 41,
                ref -> filteredRunEnds.incrementAndGet(),
                ref -> {}),
            appendContext(),
            historyInsertContext(),
            ref -> filteredRunEnds.incrementAndGet());
    DefaultStyledDocument doc = new DefaultStyledDocument();

    int nextInsertAt =
        ChatTranscriptSpoilerFlowSupport.insertSpoilerFromHistory(
            context, doc, new TargetRef("srv", "#chan"), 5, "alice", "secret", 1_000L);

    assertEquals(41, nextInsertAt);
    assertEquals(0, doc.getLength());
    assertEquals(0, filteredRunEnds.get());
  }

  @Test
  void insertSpoilerFromHistoryEnsuresTargetAndNotesEpochBeforeWriting() throws Exception {
    TargetRef ref = new TargetRef("srv", "#chan");
    Map<TargetRef, StyledDocument> docs = new HashMap<>();
    DefaultStyledDocument doc = new DefaultStyledDocument();
    AtomicInteger targetEnsures = new AtomicInteger();
    AtomicLong notedEpoch = new AtomicLong(-1L);
    AtomicInteger filteredRunEnds = new AtomicInteger();
    ChatTranscriptSpoilerFlowSupport.Context context =
        newContext(
            docs,
            target -> {
              assertEquals(ref, target);
              targetEnsures.incrementAndGet();
              docs.put(target, doc);
            },
            (target, epochMs) -> {
              assertEquals(ref, target);
              notedEpoch.set(epochMs);
            },
            new ChatTranscriptFilterRoutingSupport(
                null,
                (target, previewText, hiddenMeta, match) -> {},
                (target, insertAt, previewText, hiddenMeta, match) -> insertAt,
                target -> filteredRunEnds.incrementAndGet(),
                target -> {}),
            appendContext(),
            historyInsertContext(),
            target -> filteredRunEnds.incrementAndGet());

    int nextInsertAt =
        ChatTranscriptSpoilerFlowSupport.insertSpoilerFromHistory(
            context, ref, 0, "alice", "secret", 1_000L);

    assertTrue(nextInsertAt > 0);
    assertEquals(" \n", doc.getText(0, doc.getLength()));
    assertEquals(1, targetEnsures.get());
    assertEquals(1_000L, notedEpoch.get());
    assertEquals(1, filteredRunEnds.get());
  }

  @Test
  void insertSpoilerFromHistoryWritesVisibleSpoilerAndEndsFilteredRun() throws Exception {
    AtomicInteger filteredRunEnds = new AtomicInteger();
    ChatTranscriptSpoilerFlowSupport.Context context =
        newContext(
            new ChatTranscriptFilterRoutingSupport(
                null,
                (ref, previewText, hiddenMeta, match) -> {},
                (ref, insertAt, previewText, hiddenMeta, match) -> insertAt,
                ref -> filteredRunEnds.incrementAndGet(),
                ref -> {}),
            appendContext(),
            historyInsertContext(),
            ref -> filteredRunEnds.incrementAndGet());
    DefaultStyledDocument doc = new DefaultStyledDocument();

    int nextInsertAt =
        ChatTranscriptSpoilerFlowSupport.insertSpoilerFromHistory(
            context, doc, new TargetRef("srv", "#chan"), 0, "alice", "secret", 1_000L);

    assertTrue(nextInsertAt > 0);
    assertEquals(" \n", doc.getText(0, doc.getLength()));
    assertEquals(1, filteredRunEnds.get());
  }

  private static ChatTranscriptSpoilerFlowSupport.Context newContext(
      ChatTranscriptFilterRoutingSupport filterRoutingSupport,
      ChatTranscriptSpoilerAppendSupport.Context spoilerAppendSupportContext,
      ChatTranscriptSpoilerHistoryInsertSupport.Context spoilerHistoryInsertSupportContext,
      java.util.function.Consumer<TargetRef> filteredInsertRunEndHandler) {
    return newContext(
        new HashMap<>(),
        target -> {},
        (target, epochMs) -> {},
        filterRoutingSupport,
        spoilerAppendSupportContext,
        spoilerHistoryInsertSupportContext,
        filteredInsertRunEndHandler);
  }

  private static ChatTranscriptSpoilerFlowSupport.Context newContext(
      Map<TargetRef, StyledDocument> docs,
      Consumer<TargetRef> targetEnsureHandler,
      ObjLongConsumer<TargetRef> epochNoteHandler,
      ChatTranscriptFilterRoutingSupport filterRoutingSupport,
      ChatTranscriptSpoilerAppendSupport.Context spoilerAppendSupportContext,
      ChatTranscriptSpoilerHistoryInsertSupport.Context spoilerHistoryInsertSupportContext,
      java.util.function.Consumer<TargetRef> filteredInsertRunEndHandler) {
    return new ChatTranscriptSpoilerFlowSupport.Context(
        docs,
        targetEnsureHandler,
        epochNoteHandler,
        filterRoutingSupport,
        new ChatTranscriptSpoilerRuntimeSupport.Context(
            null,
            () -> true,
            new ChatTranscriptSpoilerRevealSupport.Context(
                new ChatStyles(null), null, null, (target, fromNick) -> fromNick),
            new Object()),
        spoilerAppendSupportContext,
        spoilerHistoryInsertSupportContext,
        filteredInsertRunEndHandler);
  }

  private static ChatTranscriptSpoilerAppendSupport.Context appendContext() {
    ChatStyles styles = new ChatStyles(null);
    return new ChatTranscriptSpoilerAppendSupport.Context(
        styles, spoilerWriteContext(), doc -> {}, (ref, doc) -> 0);
  }

  private static ChatTranscriptSpoilerHistoryInsertSupport.Context historyInsertContext() {
    return new ChatTranscriptSpoilerHistoryInsertSupport.Context(
        spoilerWriteContext(),
        (doc, insertAt) -> insertAt,
        (doc, insertAt) -> insertAt,
        (ref, insertAt, delta) -> {},
        (ref, doc) -> 0);
  }

  private static ChatTranscriptSpoilerWriteSupport.Context spoilerWriteContext() {
    ChatStyles styles = new ChatStyles(null);
    return new ChatTranscriptSpoilerWriteSupport.Context(
        styles,
        new ChatTranscriptSpoilerComponentSupport.Context(
            null, null, (target, fromNick) -> "Alice"),
        (base, filterMatch) -> new javax.swing.text.SimpleAttributeSet(base));
  }
}
