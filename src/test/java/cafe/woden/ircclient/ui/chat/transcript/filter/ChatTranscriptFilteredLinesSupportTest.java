package cafe.woden.ircclient.ui.chat.transcript.filter;

import cafe.woden.ircclient.ui.chat.transcript.line.LineMeta;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.model.FilterAction;
import cafe.woden.ircclient.model.LogDirection;
import cafe.woden.ircclient.model.LogKind;
import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.fold.FilteredFoldComponent;
import cafe.woden.ircclient.ui.chat.fold.FilteredHintComponent;
import cafe.woden.ircclient.ui.chat.fold.FilteredOverflowComponent;
import cafe.woden.ircclient.ui.filter.FilterEngine;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.text.AttributeSet;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import org.junit.jupiter.api.Test;

class ChatTranscriptFilteredLinesSupportTest {

  @Test
  void onFilteredLineAppendReusesSinglePlaceholderRow() {
    TestHarness harness = newHarness();
    ChatTranscriptFilteredLinesSupport.State state = new ChatTranscriptFilteredLinesSupport.State();
    TargetRef ref = new TargetRef("srv", "#chan");

    harness.support.onFilteredLineAppend(
        ref,
        harness.doc,
        state,
        effective(true, 10),
        "alice: one",
        lineMeta(1_000L, Set.of("tag_one")),
        hideMatch("Rule A"));
    int lenAfterFirst = harness.doc.getLength();

    harness.support.onFilteredLineAppend(
        ref,
        harness.doc,
        state,
        effective(true, 10),
        "alice: two",
        lineMeta(1_001L, Set.of("tag_two")),
        hideMatch("Rule A"));

    assertEquals(lenAfterFirst, harness.doc.getLength());
    assertEquals(1, inlineComponentCount(harness.doc, FilteredFoldComponent.class));
    assertEquals(1, harness.breakCalls.get());
  }

  @Test
  void onFilteredLineAppendUsesHintComponentWhenPlaceholdersAreDisabled() {
    TestHarness harness = newHarness();
    ChatTranscriptFilteredLinesSupport.State state = new ChatTranscriptFilteredLinesSupport.State();
    TargetRef ref = new TargetRef("srv", "#chan");

    harness.support.onFilteredLineAppend(
        ref,
        harness.doc,
        state,
        effective(false, 10),
        "alice: hidden",
        lineMeta(2_000L, Set.of("tag_hint")),
        hideMatch("Rule Hint"));

    assertEquals(1, inlineComponentCount(harness.doc, FilteredHintComponent.class));
    assertEquals(0, inlineComponentCount(harness.doc, FilteredFoldComponent.class));
    assertEquals(1, harness.breakCalls.get());
  }

  @Test
  void onFilteredLineInsertAtUsesOverflowRowAfterBatchRunCapIsExceeded() {
    TestHarness harness = newHarness();
    ChatTranscriptFilteredLinesSupport.State state = new ChatTranscriptFilteredLinesSupport.State();
    TargetRef ref = new TargetRef("srv", "#chan");
    FilterEngine.Effective effective = effective(true, 1);

    harness.support.beginHistoryInsertBatch(state, false);
    int nextInsertAt =
        harness.support.onFilteredLineInsertAt(
            ref,
            harness.doc,
            state,
            effective,
            0,
            "alice: one",
            lineMeta(3_000L, Set.of("tag_batch_one")),
            hideMatch("Rule Batch"));
    assertTrue(nextInsertAt > 0);

    harness.support.endInsertRun(state);
    harness.support.onFilteredLineInsertAt(
        ref,
        harness.doc,
        state,
        effective,
        0,
        "alice: two",
        lineMeta(3_001L, Set.of("tag_batch_two")),
        hideMatch("Rule Batch"));

    assertEquals(1, inlineComponentCount(harness.doc, FilteredFoldComponent.class));
    assertEquals(1, inlineComponentCount(harness.doc, FilteredOverflowComponent.class));
  }

  private static TestHarness newHarness() {
    ChatStyles styles = new ChatStyles(null);
    AtomicInteger breakCalls = new AtomicInteger();
    ChatTranscriptFilteredLinesSupport support =
        new ChatTranscriptFilteredLinesSupport(
            styles,
            new ChatTranscriptFilteredRunSupport.Context(
                styles, ChatTranscriptFilteredLinesSupportTest::withLineMeta),
            () -> null,
            ChatTranscriptFilteredLinesSupportTest::withLineMeta,
            doc -> {},
            (doc, insertAt) -> Math.max(0, insertAt),
            (doc, insertAt) -> Math.max(0, insertAt),
            ref -> breakCalls.incrementAndGet(),
            (ref, insertAt, delta) -> {},
            (ref, doc) -> 0);
    return new TestHarness(support, new DefaultStyledDocument(), breakCalls);
  }

  private static FilterEngine.Effective effective(
      boolean placeholdersEnabled, int maxRunsPerBatch) {
    return new FilterEngine.Effective(
        true, placeholdersEnabled, true, 3, 50, 12, maxRunsPerBatch, true);
  }

  private static FilterEngine.Match hideMatch(String ruleName) {
    return new FilterEngine.Match(UUID.randomUUID(), ruleName, FilterAction.HIDE);
  }

  private static LineMeta lineMeta(Long epochMs, Set<String> tags) {
    return new LineMeta(
        "buffer",
        LogKind.CHAT,
        LogDirection.IN,
        "alice",
        epochMs,
        tags,
        "m-" + epochMs,
        "msgid=m-" + epochMs,
        Map.of("msgid", "m-" + epochMs));
  }

  private static SimpleAttributeSet withLineMeta(AttributeSet base, LineMeta meta) {
    SimpleAttributeSet attrs = new SimpleAttributeSet(base);
    if (meta == null) {
      return attrs;
    }
    if (meta.bufferKey() != null && !meta.bufferKey().isBlank()) {
      attrs.addAttribute(ChatStyles.ATTR_META_BUFFER_KEY, meta.bufferKey());
    }
    if (meta.kind() != null) {
      attrs.addAttribute(ChatStyles.ATTR_META_KIND, meta.kind().name());
    }
    if (meta.direction() != null) {
      attrs.addAttribute(ChatStyles.ATTR_META_DIRECTION, meta.direction().name());
    }
    if (meta.fromNick() != null && !meta.fromNick().isBlank()) {
      attrs.addAttribute(ChatStyles.ATTR_META_FROM, meta.fromNick());
    }
    if (meta.epochMs() != null) {
      attrs.addAttribute(ChatStyles.ATTR_META_EPOCH_MS, meta.epochMs());
    }
    if (meta.messageId() != null && !meta.messageId().isBlank()) {
      attrs.addAttribute(ChatStyles.ATTR_META_MSGID, meta.messageId());
    }
    return attrs;
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

  private record TestHarness(
      ChatTranscriptFilteredLinesSupport support,
      DefaultStyledDocument doc,
      AtomicInteger breakCalls) {}
}
