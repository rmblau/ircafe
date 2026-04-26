package cafe.woden.ircclient.ui.chat.transcript.line;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.model.LogDirection;
import cafe.woden.ircclient.model.LogKind;
import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.fold.HistoryDividerComponent;
import cafe.woden.ircclient.ui.chat.render.ChatRichTextRenderer;
import cafe.woden.ircclient.ui.chat.transcript.filter.ChatTranscriptFilterRoutingSupport;
import cafe.woden.ircclient.ui.chat.transcript.filter.ChatTranscriptFilteredLinesSupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageCatalogSupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageStateSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTimestampFormatter;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptAuxiliaryLifecycleCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptRuntimeSettingsSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptState;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import org.junit.jupiter.api.Test;

class ChatTranscriptLineLifecycleCoordinatorTest {

  @Test
  void requiresBoundContextsBeforeUse() {
    ChatTranscriptLineLifecycleCoordinator coordinator =
        new ChatTranscriptLineLifecycleCoordinator(new Object());

    IllegalStateException error =
        assertThrows(
            IllegalStateException.class,
            () ->
                coordinator.appendLine(
                    new TargetRef("srv", "#chan"), "alice", "hello", null, null, true, null));

    assertEquals("Line coordinator context not bound", error.getMessage());
  }

  @Test
  void appendLineFlushesPendingHistoryDividerBeforeVisibleText() throws Exception {
    TestFixture fixture = new TestFixture();
    TargetRef ref = new TargetRef("srv", "#chan");

    fixture.auxiliaryLifecycleCoordinator.markHistoryDividerPending(ref, "Earlier");
    fixture.coordinator.appendLine(
        ref,
        "alice",
        "hello",
        fixture.styles.from(),
        fixture.styles.message(),
        true,
        fixture.lineMeta(ref));

    StyledDocument doc = fixture.document(ref);
    assertEquals(1, inlineComponentCount(doc, HistoryDividerComponent.class));
    assertTrue(doc.getText(0, doc.getLength()).contains("alice: hello"));
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

  private static final class TestFixture {
    private final ChatStyles styles = new ChatStyles(null);
    private final Object mutationLock = new Object();
    private final ChatTranscriptLineLifecycleCoordinator coordinator =
        new ChatTranscriptLineLifecycleCoordinator(mutationLock);
    private final ChatTranscriptAuxiliaryLifecycleCoordinator auxiliaryLifecycleCoordinator =
        new ChatTranscriptAuxiliaryLifecycleCoordinator(mutationLock);
    private final ChatTranscriptRuntimeSettingsSupport runtimeSettingsSupport =
        new ChatTranscriptRuntimeSettingsSupport(null, styles);
    private final ChatTranscriptDocumentLineSupport documentLineSupport =
        new ChatTranscriptDocumentLineSupport(styles);
    private final ChatTranscriptMessageCatalogSupport messageCatalogSupport =
        new ChatTranscriptMessageCatalogSupport(
            new ChatTranscriptMessageStateSupport.Context(120, "[redacted]", () -> 0L));
    private final Map<TargetRef, StyledDocument> docs = new HashMap<>();
    private final Map<TargetRef, ChatTranscriptState> stateByTarget = new HashMap<>();

    private TestFixture() {
      ChatTranscriptFilterRoutingSupport filterRoutingSupport =
          new ChatTranscriptFilterRoutingSupport(
              null,
              (target, previewText, hiddenMeta, match) -> {},
              (target, insertAt, previewText, hiddenMeta, match) -> insertAt,
              target -> {},
              target -> {});
      ChatTranscriptTextAppendSupport.Context textAppendSupportContext =
          new ChatTranscriptTextAppendSupport.Context(
              styles,
              new ChatTimestampFormatter(null, null),
              new ChatRichTextRenderer(null, null, styles, null),
              messageCatalogSupport,
              new ChatTranscriptManualPreviewSupport(styles, null, null),
              (ref, from) -> from,
              (base, match) -> new SimpleAttributeSet(base),
              (ref, doc) -> 0,
              auxiliaryLifecycleCoordinator::maybeRenderPendingReadMarker);
      ChatTranscriptTextInsertSupport.Context textInsertSupportContext =
          new ChatTranscriptTextInsertSupport.Context(
              styles,
              new ChatTimestampFormatter(null, null),
              new ChatRichTextRenderer(null, null, styles, null),
              messageCatalogSupport,
              (ref, from) -> from,
              (base, match) -> new SimpleAttributeSet(base),
              documentLineSupport::normalizeInsertAtLineStart,
              documentLineSupport::ensureAtLineStartForInsert,
              (ref, insertAt, delta) -> {},
              (ref, doc) -> 0);
      ChatTranscriptAuxiliaryRowsSupport auxiliaryRowsSupport =
          new ChatTranscriptAuxiliaryRowsSupport(
              styles,
              () -> null,
              (ref, epochMs) ->
                  ChatTranscriptLineMetaSupport.create(
                      ref, LogKind.STATUS, LogDirection.SYSTEM, null, epochMs, null),
              ChatTranscriptLineMetaSupport::bind,
              ChatTranscriptLineMetaSupport::withAuxiliaryRowKind,
              ChatTranscriptLineMetaSupport::withExistingMeta,
              documentLineSupport::normalizeInsertAtLineStart,
              documentLineSupport::ensureAtLineStartForInsert,
              (ref, insertAt, delta) -> {});
      auxiliaryLifecycleCoordinator.bindContext(
          docs, stateByTarget, auxiliaryRowsSupport, this::ensureTarget, target -> {});
      coordinator.bindContexts(
          docs,
          stateByTarget,
          this::ensureTarget,
          this::noteEpoch,
          filterRoutingSupport,
          target -> {},
          ref -> false,
          documentLineSupport,
          textAppendSupportContext,
          textInsertSupportContext,
          runtimeSettingsSupport,
          () -> false,
          () -> false,
          auxiliaryLifecycleCoordinator::flushPendingHistoryDividerIfNeeded);
    }

    private ChatTranscriptState newTranscriptState() {
      return new ChatTranscriptState(
          messageCatalogSupport.createState(32, 32),
          new ChatTranscriptFilteredLinesSupport.State(),
          new ChatTranscriptPresenceFoldSupport.State());
    }

    private void ensureTarget(TargetRef ref) {
      docs.computeIfAbsent(ref, ignored -> new DefaultStyledDocument());
      stateByTarget.computeIfAbsent(ref, ignored -> newTranscriptState());
    }

    private void noteEpoch(TargetRef ref, Long epochMs) {
      ChatTranscriptState state = stateByTarget.get(ref);
      if (state == null || epochMs == null) {
        return;
      }
      state.noteEpochMs(epochMs);
    }

    private LineMeta lineMeta(TargetRef ref) {
      return new LineMeta(
          ref.serverId() + "/" + ref.target(),
          LogKind.CHAT,
          LogDirection.IN,
          "alice",
          1_000L,
          Set.of(),
          "m-1",
          "",
          Map.of());
    }

    private StyledDocument document(TargetRef ref) {
      return docs.get(ref);
    }
  }
}
