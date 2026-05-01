package cafe.woden.ircclient.ui.chat.transcript.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.model.LogDirection;
import cafe.woden.ircclient.model.LogKind;
import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.render.ChatRichTextRenderer;
import cafe.woden.ircclient.ui.chat.transcript.filter.ChatTranscriptFilterRoutingSupport;
import cafe.woden.ircclient.ui.chat.transcript.filter.ChatTranscriptFilteredFlowCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.filter.ChatTranscriptFilteredLinesSupport;
import cafe.woden.ircclient.ui.chat.transcript.filter.ChatTranscriptFilteredRunSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptAuxiliaryRowsSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptDocumentLineSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptLineMetaSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptPresenceFoldSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptRenderedFromResolver;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTimestampFormatter;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptLineCapSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptRuntimeFlowCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptRuntimeSettingsSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptState;
import cafe.woden.ircclient.ui.chat.transcript.style.ChatTranscriptStyleRoutingSupport;
import java.util.HashMap;
import java.util.Map;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyledDocument;
import org.junit.jupiter.api.Test;

class ChatTranscriptMessageLineCoordinatorTest {

  @Test
  void appendNoticeAtWritesVisibleSystemLineThroughLifecycleBridge() throws Exception {
    TestFixture fixture = new TestFixture();
    TargetRef ref = new TargetRef("srv", "#chan");

    fixture.coordinator.appendNoticeAt(ref, "server", "maintenance", 2_000L);

    StyledDocument doc = fixture.document(ref);
    assertTrue(doc.getText(0, doc.getLength()).contains("server: maintenance"));
    assertEquals(
        "NOTICE",
        doc.getCharacterElement(0).getAttributes().getAttribute(ChatStyles.ATTR_META_KIND));
    assertEquals(2_000L, fixture.state(ref).earliestEpochMsSeen());
  }

  @Test
  void appendChatAtUsesRenderedFromResolverThroughTextFlow() throws Exception {
    TestFixture fixture = new TestFixture((ref, from) -> "Alice Display");
    TargetRef ref = new TargetRef("srv", "#chan");

    fixture.coordinator.appendChatAt(
        ref, "@alice:matrix", "hello", false, 3_000L, "m-1", Map.of(), null);

    assertTrue(fixture.text(ref).contains("Alice Display: hello"));
    assertEquals(3_000L, fixture.state(ref).earliestEpochMsSeen());
  }

  @Test
  void insertActionFromHistoryAtUsesRenderedFromResolverThroughActionFlow() throws Exception {
    TestFixture fixture = new TestFixture((ref, from) -> "Alice Display");
    TargetRef ref = new TargetRef("srv", "#chan");

    int nextInsertAt =
        fixture.coordinator.insertActionFromHistoryAt(
            ref, 0, "@alice:matrix", "waves", false, 4_000L, "m-2", Map.of());

    assertTrue(nextInsertAt > 0);
    assertTrue(fixture.text(ref).contains("Alice Display"));
    assertTrue(fixture.text(ref).contains("waves"));
    assertEquals(4_000L, fixture.state(ref).earliestEpochMsSeen());
  }

  private static final class TestFixture {
    private final Object mutationLock = new Object();
    private final ChatStyles styles = new ChatStyles(null);
    private final ChatTimestampFormatter timestamps = new ChatTimestampFormatter(null, null);
    private final ChatRichTextRenderer renderer =
        new ChatRichTextRenderer(null, null, styles, null);
    private final ChatTranscriptRuntimeFlowCoordinator runtimeFlowCoordinator =
        new ChatTranscriptRuntimeFlowCoordinator(mutationLock, styles);
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
            runtimeFlowCoordinator::resetAfterHeadTrim,
            ref -> runtimeFlowCoordinator.maybeRenderPendingReadMarker(ref, null));
    private final ChatTranscriptFilteredLinesSupport filteredLinesSupport =
        new ChatTranscriptFilteredLinesSupport(
            styles,
            new ChatTranscriptFilteredRunSupport.Context(
                styles, ChatTranscriptLineMetaSupport::bind),
            styleRoutingSupport::safeTranscriptFont,
            ChatTranscriptLineMetaSupport::bind,
            documentLineSupport::ensureAtLineStart,
            documentLineSupport::normalizeInsertAtLineStart,
            documentLineSupport::ensureAtLineStartForInsert,
            runtimeFlowCoordinator::breakPresenceRun,
            runtimeFlowCoordinator::shiftCurrentBlock,
            lineCapSupport::enforceTranscriptLineCap);
    private final ChatTranscriptFilteredFlowCoordinator filteredFlowCoordinator =
        new ChatTranscriptFilteredFlowCoordinator(filteredLinesSupport);
    private final ChatTranscriptFilterRoutingSupport filterRoutingSupport =
        new ChatTranscriptFilterRoutingSupport(
            null,
            filteredFlowCoordinator::onFilteredLineAppend,
            filteredFlowCoordinator::onFilteredLineInsertAt,
            filteredFlowCoordinator::endInsertRun,
            runtimeFlowCoordinator::breakPresenceRun);
    private final ChatTranscriptSenderStyleSupport.Context senderStyleSupportContext =
        new ChatTranscriptSenderStyleSupport.Context(
            styles,
            null,
            ChatTranscriptLineMetaSupport::bind,
            styleRoutingSupport::applyOutgoingLineColor,
            styleRoutingSupport::applyNotificationRuleHighlightColor);
    private final ChatTranscriptReactionSummarySupport reactionSummarySupport =
        new ChatTranscriptReactionSummarySupport(
            styles,
            styleRoutingSupport::safeTranscriptFont,
            (ref, epochMs, targetMessageId) ->
                ChatTranscriptLineMetaSupport.create(
                    ref,
                    LogKind.STATUS,
                    LogDirection.SYSTEM,
                    null,
                    epochMs,
                    null,
                    targetMessageId,
                    Map.of("draft/react", "1")),
            ChatTranscriptLineMetaSupport::bind,
            ChatTranscriptLineMetaSupport::withAuxiliaryRowKind,
            documentLineSupport::normalizeInsertAtLineStart,
            documentLineSupport::ensureAtLineStartForInsert,
            runtimeFlowCoordinator::shiftCurrentBlock);
    private final ChatTranscriptMessageLineCoordinator coordinator;

    private TestFixture() {
      this((ref, from) -> from);
    }

    private TestFixture(ChatTranscriptRenderedFromResolver renderedFromResolver) {
      coordinator =
          new ChatTranscriptMessageLineCoordinator(
              new ChatTranscriptMessageLineCoordinator.Dependencies(
                  mutationLock,
                  styles,
                  timestamps,
                  renderer,
                  null,
                  null,
                  styleRoutingSupport,
                  runtimeSettingsSupport,
                  filterRoutingSupport,
                  documentLineSupport,
                  lineCapSupport,
                  runtimeFlowCoordinator,
                  senderStyleSupportContext,
                  messageCatalogSupport,
                  reactionSummarySupport,
                  docs,
                  stateByTarget,
                  this::ensureTarget,
                  this::noteEpoch,
                  (ref, from, replyToMsgId, tsEpochMs) -> {},
                  renderedFromResolver,
                  filteredFlowCoordinator::endInsertRun,
                  filteredFlowCoordinator::shouldDeferRichTextDuringHistoryBatch));
      filteredFlowCoordinator.bindContext(
          filterRoutingSupport,
          docs,
          stateByTarget,
          this::ensureTarget,
          this::noteEpoch,
          runtimeSettingsSupport::chatHistoryDeferRichTextDuringBatch);

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
      runtimeFlowCoordinator.bindPresenceContext(
          presenceFoldSupport,
          filterRoutingSupport,
          filteredLinesSupport,
          runtimeSettingsSupport,
          docs,
          stateByTarget,
          this::ensureTarget,
          this::noteEpoch,
          () -> 7_000L);

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
              runtimeFlowCoordinator::shiftCurrentBlock);
      runtimeFlowCoordinator.bindLineLifecycleContexts(
          docs,
          stateByTarget,
          this::ensureTarget,
          this::noteEpoch,
          filterRoutingSupport,
          filteredFlowCoordinator::endInsertRun,
          filteredFlowCoordinator::shouldDeferRichTextDuringHistoryBatch,
          documentLineSupport,
          coordinator.textAppendSupportContext(),
          coordinator.textInsertSupportContext(),
          runtimeSettingsSupport,
          () -> false,
          () -> false,
          auxiliaryRowsSupport,
          filteredFlowCoordinator::endAppendRun);
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

    private StyledDocument document(TargetRef ref) {
      return docs.get(ref);
    }

    private String text(TargetRef ref) throws Exception {
      StyledDocument doc = document(ref);
      return doc == null ? "" : doc.getText(0, doc.getLength());
    }

    private ChatTranscriptState state(TargetRef ref) {
      return stateByTarget.get(ref);
    }
  }
}
