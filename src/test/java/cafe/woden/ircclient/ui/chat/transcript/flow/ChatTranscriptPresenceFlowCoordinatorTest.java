package cafe.woden.ircclient.ui.chat.transcript.flow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cafe.woden.ircclient.app.api.PresenceEvent;
import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.fold.PresenceFoldComponent;
import cafe.woden.ircclient.ui.chat.render.ChatRichTextRenderer;
import cafe.woden.ircclient.ui.chat.transcript.line.LineMeta;
import cafe.woden.ircclient.ui.chat.transcript.filter.ChatTranscriptFilterRoutingSupport;
import cafe.woden.ircclient.ui.chat.transcript.filter.ChatTranscriptFilteredLinesSupport;
import cafe.woden.ircclient.ui.chat.transcript.filter.ChatTranscriptFilteredRunSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptLineMetaSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptPresenceFoldSupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageCatalogSupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageStateSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptRuntimeSettingsSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptState;
import cafe.woden.ircclient.ui.filter.FilterEngine;
import java.util.HashMap;
import java.util.Map;
import javax.swing.text.AttributeSet;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import org.junit.jupiter.api.Test;

class ChatTranscriptPresenceFlowCoordinatorTest {

  @Test
  void requiresBoundContextBeforeUse() {
    ChatTranscriptPresenceFlowCoordinator coordinator =
        new ChatTranscriptPresenceFlowCoordinator(new ChatStyles(null));

    IllegalStateException error =
        assertThrows(
            IllegalStateException.class,
            () ->
                coordinator.appendPresence(new TargetRef("srv", "#chan"), PresenceEvent.join("a")));

    assertEquals("Presence flow coordinator context not bound", error.getMessage());
  }

  @Test
  void resetAfterHeadTrimClearsEarliestEpochAndStartsFreshPresenceBlock() {
    TestFixture fixture = new TestFixture();
    TargetRef ref = new TargetRef("srv", "#chan");
    fixture.ensureTarget(ref);
    fixture.noteEpoch(ref, 1_000L);

    fixture.coordinator.appendPresence(ref, PresenceEvent.join("alice"));
    fixture.coordinator.appendPresence(ref, PresenceEvent.part("bob", "bye"));

    PresenceFoldComponent folded =
        assertInstanceOf(PresenceFoldComponent.class, lineComponent(fixture.document(ref), 0));
    assertEquals(1, lineCount(fixture.document(ref)));

    fixture.coordinator.resetAfterHeadTrim(ref);
    assertNull(fixture.state(ref).earliestEpochMsSeen());

    fixture.coordinator.appendPresence(ref, PresenceEvent.quit("carol", "gone"));

    assertSame(folded, lineComponent(fixture.document(ref), 0));
    assertEquals(2, lineCount(fixture.document(ref)));
  }

  private static java.awt.Component lineComponent(StyledDocument doc, int lineIndex) {
    Element root = doc.getDefaultRootElement();
    if (root == null || lineIndex < 0 || lineIndex >= root.getElementCount()) {
      return null;
    }
    Element line = root.getElement(lineIndex);
    if (line == null) {
      return null;
    }
    int start = Math.max(0, line.getStartOffset());
    if (start >= doc.getLength()) {
      return null;
    }
    AttributeSet attrs = doc.getCharacterElement(start).getAttributes();
    return StyleConstants.getComponent(attrs);
  }

  private static int lineCount(StyledDocument doc) {
    try {
      return (int) doc.getText(0, doc.getLength()).chars().filter(ch -> ch == '\n').count();
    } catch (Exception ignored) {
      return 0;
    }
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

  private static final class TestFixture {
    private final ChatStyles styles = new ChatStyles(null);
    private final FilterEngine filterEngine = new FilterEngine(null);
    private final ChatTranscriptRuntimeSettingsSupport runtimeSettingsSupport =
        new ChatTranscriptRuntimeSettingsSupport(null, styles);
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
            ChatTranscriptPresenceFlowCoordinatorTest::ensureAtLineStart,
            (doc, insertAt) -> Math.max(0, insertAt),
            (doc, insertAt) -> Math.max(0, insertAt),
            target -> {},
            (target, insertAt, delta) -> {},
            (target, doc) -> 0);
    private final ChatTranscriptPresenceFlowCoordinator coordinator =
        new ChatTranscriptPresenceFlowCoordinator(styles);
    private final ChatTranscriptPresenceFoldSupport presenceFoldSupport =
        new ChatTranscriptPresenceFoldSupport(
            styles,
            new ChatRichTextRenderer(null, null, styles, null),
            null,
            ChatTranscriptLineMetaSupport::bind,
            ChatTranscriptLineMetaSupport::withExistingMeta,
            (base, match) -> new SimpleAttributeSet(base),
            ChatTranscriptPresenceFlowCoordinatorTest::ensureAtLineStart,
            (target, doc) -> 0);
    private final ChatTranscriptFilterRoutingSupport filterRoutingSupport =
        new ChatTranscriptFilterRoutingSupport(
            filterEngine,
            (target, previewText, hiddenMeta, match) -> {},
            (target, insertAt, previewText, hiddenMeta, match) -> insertAt,
            target -> {},
            target -> {});
    private final Map<TargetRef, StyledDocument> docs = new HashMap<>();
    private final Map<TargetRef, ChatTranscriptState> stateByTarget = new HashMap<>();

    private TestFixture() {
      coordinator.bindContext(
          presenceFoldSupport,
          filterRoutingSupport,
          filteredLinesSupport,
          runtimeSettingsSupport,
          docs,
          stateByTarget,
          this::ensureTarget,
          this::noteEpoch,
          this::appendLine,
          this::insertLine,
          () -> 5_000L);
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

    void appendLine(
        TargetRef ref,
        String from,
        String text,
        AttributeSet fromStyle,
        AttributeSet msgStyle,
        boolean allowEmbeds,
        LineMeta meta) {
      try {
        StyledDocument doc = docs.get(ref);
        if (doc != null) {
          doc.insertString(doc.getLength(), text + "\n", new SimpleAttributeSet(msgStyle));
        }
      } catch (Exception ignored) {
      }
    }

    int insertLine(
        TargetRef ref,
        int insertAt,
        String from,
        String text,
        AttributeSet fromStyle,
        AttributeSet msgStyle,
        LineMeta meta) {
      try {
        StyledDocument doc = docs.get(ref);
        if (doc != null) {
          int safeInsertAt = Math.max(0, Math.min(insertAt, doc.getLength()));
          doc.insertString(safeInsertAt, text + "\n", new SimpleAttributeSet(msgStyle));
          return safeInsertAt + text.length() + 1;
        }
      } catch (Exception ignored) {
      }
      return Math.max(0, insertAt);
    }

    ChatTranscriptState state(TargetRef ref) {
      return stateByTarget.get(ref);
    }

    StyledDocument document(TargetRef ref) {
      return docs.get(ref);
    }
  }
}
