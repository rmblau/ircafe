package cafe.woden.ircclient.ui.chat.transcript.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.app.api.PresenceEvent;
import cafe.woden.ircclient.model.LogDirection;
import cafe.woden.ircclient.model.LogKind;
import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.fold.HistoryDividerComponent;
import cafe.woden.ircclient.ui.chat.fold.PresenceFoldComponent;
import cafe.woden.ircclient.ui.chat.render.ChatRichTextRenderer;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTimestampFormatter;
import cafe.woden.ircclient.ui.chat.transcript.line.LineMeta;
import cafe.woden.ircclient.ui.chat.transcript.filter.ChatTranscriptFilterRoutingSupport;
import cafe.woden.ircclient.ui.chat.transcript.filter.ChatTranscriptFilteredLinesSupport;
import cafe.woden.ircclient.ui.chat.transcript.filter.ChatTranscriptFilteredRunSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptAuxiliaryRowsSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptDocumentLineSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptLineMetaSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptManualPreviewSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptPresenceFoldSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptTextAppendSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptTextInsertSupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageCatalogSupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageStateSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptLineCapSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptRuntimeSettingsSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptState;
import cafe.woden.ircclient.ui.chat.transcript.style.ChatTranscriptStyleRoutingSupport;
import java.awt.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import org.junit.jupiter.api.Test;

class ChatTranscriptRuntimeFlowCoordinatorTest {

  @Test
  void appendLineFlushesPendingHistoryDividerBeforeVisibleText() throws Exception {
    TestFixture fixture = new TestFixture();
    TargetRef ref = new TargetRef("srv", "#chan");

    fixture.coordinator.markHistoryDividerPending(ref, "Earlier");
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

  @Test
  void appendPresenceUsesBoundPresenceFlow() {
    TestFixture fixture = new TestFixture();
    TargetRef ref = new TargetRef("srv", "#chan");

    fixture.coordinator.appendPresence(ref, PresenceEvent.join("alice"));
    fixture.coordinator.appendPresence(ref, PresenceEvent.part("bob", "bye"));

    assertInstanceOf(PresenceFoldComponent.class, lineComponent(fixture.document(ref), 0));
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

  private static Component lineComponent(StyledDocument doc, int lineIndex) {
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
    return StyleConstants.getComponent(doc.getCharacterElement(start).getAttributes());
  }

  private static final class TestFixture {
    private final ChatStyles styles = new ChatStyles(null);
    private final ChatTimestampFormatter timestamps = new ChatTimestampFormatter(null, null);
    private final ChatRichTextRenderer renderer =
        new ChatRichTextRenderer(null, null, styles, null);
    private final ChatTranscriptRuntimeFlowCoordinator coordinator =
        new ChatTranscriptRuntimeFlowCoordinator(new Object(), styles);
    private final ChatTranscriptRuntimeSettingsSupport runtimeSettingsSupport =
        new ChatTranscriptRuntimeSettingsSupport(null, styles);
    private final ChatTranscriptStyleRoutingSupport styleRoutingSupport =
        new ChatTranscriptStyleRoutingSupport(
            styles,
            runtimeSettingsSupport::safeSettings,
            runtimeSettingsSupport::configuredOutgoingLineColor);
    private final ChatTranscriptDocumentLineSupport documentLineSupport =
        new ChatTranscriptDocumentLineSupport(styles);
    private final ChatTranscriptMessageCatalogSupport messageCatalogSupport =
        new ChatTranscriptMessageCatalogSupport(
            new ChatTranscriptMessageStateSupport.Context(120, "[redacted]", () -> 0L));
    private final Map<TargetRef, StyledDocument> docs = new HashMap<>();
    private final Map<TargetRef, ChatTranscriptState> stateByTarget = new HashMap<>();
    private final ChatTranscriptLineCapSupport lineCapSupport =
        new ChatTranscriptLineCapSupport(
            runtimeSettingsSupport::transcriptMaxLinesPerTarget,
            coordinator::resetAfterHeadTrim,
            ref -> coordinator.maybeRenderPendingReadMarker(ref, null));

    private TestFixture() {
      ChatTranscriptFilteredLinesSupport filteredLinesSupport =
          new ChatTranscriptFilteredLinesSupport(
              styles,
              new ChatTranscriptFilteredRunSupport.Context(
                  styles, ChatTranscriptLineMetaSupport::bind),
              styleRoutingSupport::safeTranscriptFont,
              ChatTranscriptLineMetaSupport::bind,
              documentLineSupport::ensureAtLineStart,
              documentLineSupport::normalizeInsertAtLineStart,
              documentLineSupport::ensureAtLineStartForInsert,
              coordinator::breakPresenceRun,
              coordinator::shiftCurrentBlock,
              lineCapSupport::enforceTranscriptLineCap);
      ChatTranscriptFilterRoutingSupport filterRoutingSupport =
          new ChatTranscriptFilterRoutingSupport(
              null,
              (target, previewText, hiddenMeta, match) -> {},
              (target, insertAt, previewText, hiddenMeta, match) -> insertAt,
              target -> {},
              coordinator::breakPresenceRun);
      ChatTranscriptTextAppendSupport.Context textAppendSupportContext =
          new ChatTranscriptTextAppendSupport.Context(
              styles,
              timestamps,
              renderer,
              messageCatalogSupport,
              new ChatTranscriptManualPreviewSupport(styles, null, null),
              (ref, from) -> from,
              styleRoutingSupport::withFilterMatch,
              lineCapSupport::enforceTranscriptLineCap,
              coordinator::maybeRenderPendingReadMarker);
      ChatTranscriptTextInsertSupport.Context textInsertSupportContext =
          new ChatTranscriptTextInsertSupport.Context(
              styles,
              timestamps,
              renderer,
              messageCatalogSupport,
              (ref, from) -> from,
              styleRoutingSupport::withFilterMatch,
              documentLineSupport::normalizeInsertAtLineStart,
              documentLineSupport::ensureAtLineStartForInsert,
              coordinator::shiftCurrentBlock,
              lineCapSupport::enforceTranscriptLineCap);
      ChatTranscriptAuxiliaryRowsSupport auxiliaryRowsSupport =
          new ChatTranscriptAuxiliaryRowsSupport(
              styles,
              styleRoutingSupport::safeTranscriptFont,
              (ref, epochMs) ->
                  ChatTranscriptLineMetaSupport.create(
                      ref, LogKind.STATUS, LogDirection.SYSTEM, null, epochMs, null),
              ChatTranscriptLineMetaSupport::bind,
              ChatTranscriptLineMetaSupport::withAuxiliaryRowKind,
              ChatTranscriptLineMetaSupport::withExistingMeta,
              documentLineSupport::normalizeInsertAtLineStart,
              documentLineSupport::ensureAtLineStartForInsert,
              coordinator::shiftCurrentBlock);
      ChatTranscriptPresenceFoldSupport presenceFoldSupport =
          new ChatTranscriptPresenceFoldSupport(
              styles,
              renderer,
              timestamps,
              ChatTranscriptLineMetaSupport::bind,
              ChatTranscriptLineMetaSupport::withExistingMeta,
              styleRoutingSupport::withFilterMatch,
              documentLineSupport::ensureAtLineStart,
              lineCapSupport::enforceTranscriptLineCap);

      coordinator.bindLineLifecycleContexts(
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
          auxiliaryRowsSupport,
          target -> {});
      coordinator.bindPresenceContext(
          presenceFoldSupport,
          filterRoutingSupport,
          filteredLinesSupport,
          runtimeSettingsSupport,
          docs,
          stateByTarget,
          this::ensureTarget,
          this::noteEpoch,
          () -> 5_000L);
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

    private ChatTranscriptState newTranscriptState() {
      return new ChatTranscriptState(
          messageCatalogSupport.createState(32, 32),
          new ChatTranscriptFilteredLinesSupport.State(),
          new ChatTranscriptPresenceFoldSupport.State());
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
