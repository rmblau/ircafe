package cafe.woden.ircclient.ui.chat.transcript.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cafe.woden.ircclient.model.LogDirection;
import cafe.woden.ircclient.model.LogKind;
import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.fold.HistoryDividerComponent;
import cafe.woden.ircclient.ui.chat.transcript.filter.ChatTranscriptFilteredLinesSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptAuxiliaryRowsSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptDocumentLineSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptLineMetaSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptPresenceFoldSupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageCatalogSupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageStateSupport;
import java.util.HashMap;
import java.util.Map;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import org.junit.jupiter.api.Test;

class ChatTranscriptAuxiliaryLifecycleCoordinatorTest {

  @Test
  void requiresBoundContextBeforeUse() {
    ChatTranscriptAuxiliaryLifecycleCoordinator coordinator =
        new ChatTranscriptAuxiliaryLifecycleCoordinator(new Object());

    IllegalStateException error =
        assertThrows(
            IllegalStateException.class,
            () -> coordinator.ensureLoadOlderMessagesControl(new TargetRef("srv", "#chan")));

    assertEquals("Auxiliary lifecycle coordinator context not bound", error.getMessage());
  }

  @Test
  void flushesPendingHistoryDividerWhenRequested() {
    TestFixture fixture = new TestFixture();
    TargetRef ref = new TargetRef("srv", "#chan");
    fixture.ensureTarget(ref);
    StyledDocument doc = fixture.docs.get(ref);

    fixture.coordinator.markHistoryDividerPending(ref, "Earlier");
    fixture.coordinator.flushPendingHistoryDividerIfNeeded(ref, doc);

    assertEquals(1, inlineComponentCount(doc, HistoryDividerComponent.class));
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
    private final ChatTranscriptAuxiliaryLifecycleCoordinator coordinator =
        new ChatTranscriptAuxiliaryLifecycleCoordinator(new Object());
    private final ChatTranscriptDocumentLineSupport documentLineSupport =
        new ChatTranscriptDocumentLineSupport(styles);
    private final ChatTranscriptMessageCatalogSupport messageCatalogSupport =
        new ChatTranscriptMessageCatalogSupport(
            new ChatTranscriptMessageStateSupport.Context(120, "[redacted]", () -> 0L));
    private final Map<TargetRef, StyledDocument> docs = new HashMap<>();
    private final Map<TargetRef, ChatTranscriptState> stateByTarget = new HashMap<>();
    private TestFixture() {
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
      coordinator.bindContext(
          docs,
          stateByTarget,
          auxiliaryRowsSupport,
          this::ensureTarget,
          target -> {});
    }

    private void ensureTarget(TargetRef ref) {
      docs.computeIfAbsent(ref, ignored -> new DefaultStyledDocument());
      stateByTarget.computeIfAbsent(ref, ignored -> newTranscriptState());
    }

    private ChatTranscriptState newTranscriptState() {
      return new ChatTranscriptState(
          messageCatalogSupport.createState(32, 32),
          new ChatTranscriptFilteredLinesSupport.State(),
          new ChatTranscriptPresenceFoldSupport.State());
    }
  }
}
