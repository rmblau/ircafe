package cafe.woden.ircclient.ui.chat.transcript.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.model.FilterAction;
import cafe.woden.ircclient.model.LogDirection;
import cafe.woden.ircclient.model.LogKind;
import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.fold.FilteredFoldComponent;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptLineMetaSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptPresenceFoldSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.LineMeta;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageCatalogSupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageStateSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptState;
import cafe.woden.ircclient.ui.filter.FilterEngine;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import org.junit.jupiter.api.Test;

class ChatTranscriptFilteredFlowSupportTest {

  private final ChatTranscriptFilteredFlowSupport support = new ChatTranscriptFilteredFlowSupport();

  @Test
  void beginHistoryInsertBatchHonorsForcedDeferralFlag() {
    TestFixture fixture = new TestFixture();
    TargetRef ref = new TargetRef("srv", "#chan");

    fixture.ensureTarget(ref);
    assertFalse(support.shouldDeferRichTextDuringHistoryBatch(fixture.context(), ref));

    support.beginHistoryInsertBatch(fixture.context(), ref, true);

    assertTrue(support.shouldDeferRichTextDuringHistoryBatch(fixture.context(), ref));

    support.endHistoryInsertBatch(fixture.context(), ref);
    assertFalse(support.shouldDeferRichTextDuringHistoryBatch(fixture.context(), ref));
  }

  @Test
  void beginHistoryInsertBatchFallsBackToUiDefaultWhenEnabled() {
    TestFixture fixture = new TestFixture();
    TargetRef ref = new TargetRef("srv", "#chan");
    fixture.defaultDeferRichTextDuringBatch.set(true);
    fixture.ensureTarget(ref);

    support.beginHistoryInsertBatch(fixture.context(), ref, false);

    assertTrue(support.shouldDeferRichTextDuringHistoryBatch(fixture.context(), ref));
  }

  @Test
  void hiddenAppendDelegatesToFilteredLinesSupportAndNotesEpoch() {
    TestFixture fixture = new TestFixture();
    TargetRef ref = new TargetRef("srv", "#chan");
    fixture.ensureTarget(ref);

    LineMeta hiddenMeta =
        new LineMeta(
            "buffer",
            LogKind.CHAT,
            LogDirection.IN,
            "alice",
            1_234L,
            Set.of("tag_hidden"),
            "m-1234",
            "msgid=m-1234",
            Map.of("msgid", "m-1234"));

    support.onFilteredLineAppend(
        fixture.context(),
        ref,
        "alice: hidden",
        hiddenMeta,
        new FilterEngine.Match(UUID.randomUUID(), "Rule A", FilterAction.HIDE));

    assertEquals(1_234L, fixture.state(ref).earliestEpochMsSeen());
    assertEquals(1, inlineComponentCount(fixture.document(ref), FilteredFoldComponent.class));
  }

  @Test
  void hiddenInsertDelegatesToFilteredLinesSupportAndReturnsAdvancedOffset() {
    TestFixture fixture = new TestFixture();
    TargetRef ref = new TargetRef("srv", "#chan");
    fixture.ensureTarget(ref);

    int nextInsertAt =
        support.onFilteredLineInsertAt(
            fixture.context(),
            ref,
            0,
            "alice: hidden",
            new LineMeta(
                "buffer",
                LogKind.CHAT,
                LogDirection.IN,
                "alice",
                2_468L,
                Set.of("tag_hidden_insert"),
                "m-2468",
                "msgid=m-2468",
                Map.of("msgid", "m-2468")),
            new FilterEngine.Match(UUID.randomUUID(), "Rule B", FilterAction.HIDE));

    assertTrue(nextInsertAt > 0);
    assertEquals(2_468L, fixture.state(ref).earliestEpochMsSeen());
    assertEquals(1, inlineComponentCount(fixture.document(ref), FilteredFoldComponent.class));
  }

  private static int inlineComponentCount(StyledDocument doc, Class<?> componentType) {
    Element root = doc.getDefaultRootElement();
    if (root == null) {
      return 0;
    }
    int count = 0;
    int len = doc.getLength();
    for (int i = 0; i < root.getElementCount(); i++) {
      Element line = root.getElement(i);
      if (line == null) {
        continue;
      }
      int start = Math.max(0, line.getStartOffset());
      if (start >= len) {
        continue;
      }
      Object component =
          StyleConstants.getComponent(doc.getCharacterElement(start).getAttributes());
      if (componentType.isInstance(component)) {
        count++;
      }
    }
    return count;
  }

  private static void ensureAtLineStart(StyledDocument doc) {
    if (doc == null) {
      return;
    }
    int len = doc.getLength();
    if (len <= 0) {
      return;
    }
    try {
      if (!"\n".equals(doc.getText(len - 1, 1))) {
        doc.insertString(len, "\n", new SimpleAttributeSet());
      }
    } catch (Exception ignored) {
    }
  }

  private final class TestFixture {
    private final ChatStyles styles = new ChatStyles(null);
    private final AtomicBoolean defaultDeferRichTextDuringBatch = new AtomicBoolean(false);
    private final ChatTranscriptMessageCatalogSupport messageCatalogSupport =
        new ChatTranscriptMessageCatalogSupport(
            new ChatTranscriptMessageStateSupport.Context(120, "[redacted]", () -> 0L));
    private final ChatTranscriptFilteredLinesSupport filteredLinesSupport =
        new ChatTranscriptFilteredLinesSupport(
            styles,
            new ChatTranscriptFilteredRunSupport.Context(
                styles, ChatTranscriptLineMetaSupport::bind),
            () -> null,
            ChatTranscriptLineMetaSupport::bind,
            ChatTranscriptFilteredFlowSupportTest::ensureAtLineStart,
            (doc, insertAt) -> Math.max(0, insertAt),
            (doc, insertAt) -> Math.max(0, insertAt),
            target -> {},
            (target, insertAt, delta) -> {},
            (target, doc) -> 0);
    private final ChatTranscriptFilterRoutingSupport filterRoutingSupport =
        new ChatTranscriptFilterRoutingSupport(
            null,
            (target, previewText, hiddenMeta, match) -> {},
            (target, insertAt, previewText, hiddenMeta, match) -> insertAt,
            target -> {},
            target -> {});
    private final Map<TargetRef, StyledDocument> docs = new HashMap<>();
    private final Map<TargetRef, ChatTranscriptState> stateByTarget = new HashMap<>();

    ChatTranscriptFilteredFlowSupport.Context context() {
      return new ChatTranscriptFilteredFlowSupport.Context(
          filteredLinesSupport,
          filterRoutingSupport,
          docs,
          stateByTarget,
          this::ensureTarget,
          this::noteEpoch,
          defaultDeferRichTextDuringBatch::get);
    }

    void ensureTarget(TargetRef ref) {
      docs.computeIfAbsent(ref, key -> new DefaultStyledDocument());
      stateByTarget.computeIfAbsent(
          ref,
          key ->
              new ChatTranscriptState(
                  messageCatalogSupport.createState(32, 32),
                  new ChatTranscriptFilteredLinesSupport.State(),
                  new ChatTranscriptPresenceFoldSupport.State()));
    }

    void noteEpoch(TargetRef ref, Long epochMs) {
      ChatTranscriptState state = stateByTarget.get(ref);
      if (state == null || epochMs == null) {
        return;
      }
      state.noteEpochMs(epochMs);
    }

    ChatTranscriptState state(TargetRef ref) {
      return stateByTarget.get(ref);
    }

    StyledDocument document(TargetRef ref) {
      return docs.get(ref);
    }
  }
}
