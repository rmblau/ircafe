package cafe.woden.ircclient.ui.chat.transcript;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

import cafe.woden.ircclient.app.api.PresenceEvent;
import cafe.woden.ircclient.model.LogKind;
import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.fold.PresenceFoldComponent;
import cafe.woden.ircclient.ui.chat.render.ChatRichTextRenderer;
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

class ChatTranscriptPresenceFlowSupportTest {

  private final ChatStyles styles = new ChatStyles(null);
  private final ChatTranscriptPresenceFlowSupport support =
      new ChatTranscriptPresenceFlowSupport(styles);

  @Test
  void appendPresenceStartsNewFoldBlockAfterPresenceRunBreak() {
    TestFixture fixture = new TestFixture();
    TargetRef ref = new TargetRef("srv", "#chan");
    fixture.ensureTarget(ref);

    support.appendPresence(fixture.context(), ref, PresenceEvent.join("alice"));
    support.appendPresence(fixture.context(), ref, PresenceEvent.part("bob", "bye"));

    PresenceFoldComponent folded =
        assertInstanceOf(PresenceFoldComponent.class, lineComponent(fixture.document(ref), 0));
    assertEquals(1, lineCount(fixture.document(ref)));

    support.breakPresenceRun(fixture.context(), ref);
    support.appendPresence(fixture.context(), ref, PresenceEvent.quit("carol", "gone"));

    assertSame(folded, lineComponent(fixture.document(ref), 0));
    assertEquals(2, lineCount(fixture.document(ref)));
  }

  @Test
  void insertPresenceFromHistoryDelegatesUsingStatusStyles() {
    TestFixture fixture = new TestFixture();
    TargetRef ref = new TargetRef("srv", "#chan");
    fixture.ensureTarget(ref);

    final AttributeSet[] capturedFromStyle = new AttributeSet[1];
    final AttributeSet[] capturedMessageStyle = new AttributeSet[1];
    final LineMeta[] capturedMeta = new LineMeta[1];

    ChatTranscriptPresenceFlowSupport.Context context =
        fixture.context(
            (target, insertAt, from, text, fromStyle, msgStyle, meta) -> {
              capturedFromStyle[0] = fromStyle;
              capturedMessageStyle[0] = msgStyle;
              capturedMeta[0] = meta;
              return 77;
            });

    int result = support.insertPresenceFromHistoryAt(context, ref, 12, "alice joined", 1_234L);

    assertEquals(77, result);
    assertSame(styles.status(), capturedFromStyle[0]);
    assertSame(styles.status(), capturedMessageStyle[0]);
    assertEquals(LogKind.PRESENCE, capturedMeta[0].kind());
    assertEquals(1_234L, capturedMeta[0].epochMs());
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

  private final class TestFixture {
    private final FilterEngine filterEngine = new FilterEngine(null);
    private final ChatTranscriptRuntimeSettingsSupport runtimeSettingsSupport =
        new ChatTranscriptRuntimeSettingsSupport(null, styles);
    private final ChatTranscriptMessageCatalogSupport messageCatalogSupport =
        new ChatTranscriptMessageCatalogSupport(
            new ChatTranscriptMessageStateSupport.Context(120, "[redacted]", () -> 0L));
    private final ChatTranscriptFilteredRunSupport.Context filteredRunContext =
        new ChatTranscriptFilteredRunSupport.Context(styles, ChatTranscriptLineMetaSupport::bind);
    private final ChatTranscriptFilteredLinesSupport filteredLinesSupport =
        new ChatTranscriptFilteredLinesSupport(
            styles,
            filteredRunContext,
            () -> null,
            ChatTranscriptLineMetaSupport::bind,
            ChatTranscriptPresenceFlowSupportTest::ensureAtLineStart,
            (doc, insertAt) -> Math.max(0, insertAt),
            (doc, insertAt) -> Math.max(0, insertAt),
            target -> {},
            (target, insertAt, delta) -> {},
            (target, doc) -> 0);
    private final ChatTranscriptPresenceFoldSupport presenceFoldSupport =
        new ChatTranscriptPresenceFoldSupport(
            styles,
            new ChatRichTextRenderer(null, null, styles, null),
            null,
            ChatTranscriptLineMetaSupport::bind,
            ChatTranscriptLineMetaSupport::withExistingMeta,
            (base, match) -> new SimpleAttributeSet(base),
            ChatTranscriptPresenceFlowSupportTest::ensureAtLineStart,
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

    ChatTranscriptPresenceFlowSupport.Context context() {
      return context((target, insertAt, from, text, fromStyle, msgStyle, meta) -> insertAt);
    }

    ChatTranscriptPresenceFlowSupport.Context context(
        ChatTranscriptPresenceFlowSupport.InsertLineHandler insertLineHandler) {
      return new ChatTranscriptPresenceFlowSupport.Context(
          filterRoutingSupport,
          presenceFoldSupport,
          filteredLinesSupport,
          runtimeSettingsSupport,
          docs,
          stateByTarget,
          this::ensureTarget,
          this::noteEpoch,
          (target, from, text, fromStyle, msgStyle, allowEmbeds, meta) -> {
            try {
              StyledDocument doc = docs.get(target);
              if (doc != null) {
                doc.insertString(doc.getLength(), text + "\n", new SimpleAttributeSet(msgStyle));
              }
            } catch (Exception ignored) {
            }
          },
          insertLineHandler,
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
      Long current = state.earliestEpochMsSeen;
      if (current == null || epochMs < current) {
        state.earliestEpochMsSeen = epochMs;
      }
    }

    StyledDocument document(TargetRef ref) {
      return docs.get(ref);
    }

  }
}
