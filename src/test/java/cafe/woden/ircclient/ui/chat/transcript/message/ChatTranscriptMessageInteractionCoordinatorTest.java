package cafe.woden.ircclient.ui.chat.transcript.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import cafe.woden.ircclient.model.LogDirection;
import cafe.woden.ircclient.model.LogKind;
import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.transcript.filter.ChatTranscriptFilteredLinesSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptLineMetaSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptPresenceFoldSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptState;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledDocument;
import org.junit.jupiter.api.Test;

class ChatTranscriptMessageInteractionCoordinatorTest {

  @Test
  void applyMessageEditUpdatesDocumentAndPreviewThroughSharedCatalogState() throws Exception {
    TestFixture fixture = new TestFixture();
    TargetRef ref = new TargetRef("srv", "#chan");
    fixture.ensureTarget(ref);
    fixture.insertMessageLine(ref, "alice", "hello", "m-1");

    boolean applied =
        fixture.coordinator.applyMessageEdit(
            ref, "m-1", "updated", "alice", 2_000L, "m-2", Map.of("msgid", "m-2"));

    assertTrue(applied);
    assertEquals(
        "alice: updated (edited)\n",
        fixture.document(ref).getText(0, fixture.document(ref).getLength()));
    assertEquals("alice: updated (edited)", fixture.coordinator.messagePreviewById(ref, "m-1"));
    assertEquals(0, fixture.coordinator.messageOffsetById(ref, "m-1"));
  }

  @Test
  void messageOffsetAndOwnMessageUseDocumentMetadata() throws Exception {
    TestFixture fixture = new TestFixture();
    TargetRef ref = new TargetRef("srv", "#chan");
    fixture.ensureTarget(ref);
    fixture.insertMessageLine(ref, "alice", "hello", "m-1", LogDirection.OUT, true);

    assertEquals(0, fixture.coordinator.messageOffsetById(ref, "m-1"));
    assertTrue(fixture.coordinator.isOwnMessage(ref, "m-1"));
    assertFalse(fixture.coordinator.isOwnMessage(ref, "missing"));
  }

  @Test
  void messagePreviewUsesCatalogState() {
    TestFixture fixture = new TestFixture();
    TargetRef ref = new TargetRef("srv", "#chan");
    fixture.ensureTarget(ref);
    fixture.messageCatalogSupport.recordInsertedMessage(
        fixture.state(ref).messageCatalog(),
        ChatTranscriptLineMetaSupport.create(
            ref, LogKind.CHAT, LogDirection.IN, "alice", 321L, null, "m-2", Map.of()),
        "alice",
        "hello there");

    assertEquals("alice: hello there", fixture.coordinator.messagePreviewById(ref, "m-2"));
    assertEquals("", fixture.coordinator.messagePreviewById(ref, "missing"));
  }

  @Test
  void applyMessageReactionEnsuresTargetAndDelegatesReactionState() {
    ChatTranscriptReactionSummarySupport reactionSummarySupport =
        mock(ChatTranscriptReactionSummarySupport.class);
    TestFixture fixture = new TestFixture(reactionSummarySupport);
    TargetRef ref = new TargetRef("srv", "#chan");
    fixture.ensureTarget(ref);

    fixture.coordinator.applyMessageReaction(ref, "m-1", ":)", "alice", 10L);

    assertTrue(fixture.ensured.get());
    verify(reactionSummarySupport)
        .applyMessageReaction(
            ref,
            fixture.document(ref),
            fixture.state(ref).reactionSummary(),
            "m-1",
            ":)",
            "alice",
            10L);
  }

  @Test
  void setReactionChipActionHandlerRebindsExistingStates() {
    ChatTranscriptReactionSummarySupport reactionSummarySupport =
        mock(ChatTranscriptReactionSummarySupport.class);
    TestFixture fixture = new TestFixture(reactionSummarySupport);
    TargetRef ref = new TargetRef("srv", "#chan");
    fixture.ensureTarget(ref);

    ReactionChipActionHandler handler = (target, messageId, reactionToken, unreactRequested) -> {};
    fixture.coordinator.setReactionChipActionHandler(handler);

    verify(reactionSummarySupport).setReactionChipActionHandler(eq(handler), anyMap());
  }

  @Test
  void missingStateReturnsFalseForReactionLookup() {
    TestFixture fixture = new TestFixture(mock(ChatTranscriptReactionSummarySupport.class));

    assertFalse(
        fixture.coordinator.hasReactionFromNick(
            new TargetRef("srv", "#chan"), "m-1", ":)", "alice"));
  }

  @Test
  void applyMessageRedactionReturnsFalseWhenRefMissing() {
    TestFixture fixture = new TestFixture();

    assertFalse(
        fixture.coordinator.applyMessageRedaction(
            null, "m-1", "alice", 12L, "m-2", Map.of("msgid", "m-2")));
  }

  private static final class TestFixture {
    private final ChatStyles styles = new ChatStyles(null);
    private final ChatTranscriptMessageCatalogSupport messageCatalogSupport =
        new ChatTranscriptMessageCatalogSupport(
            new ChatTranscriptMessageStateSupport.Context(120, "[redacted]", () -> 1L));
    private final Map<TargetRef, StyledDocument> docs = new HashMap<>();
    private final Map<TargetRef, ChatTranscriptState> stateByTarget = new HashMap<>();
    private final AtomicBoolean ensured = new AtomicBoolean(false);
    private final ChatTranscriptMessageInteractionCoordinator coordinator;

    private TestFixture() {
      this(mock(ChatTranscriptReactionSummarySupport.class));
    }

    private TestFixture(ChatTranscriptReactionSummarySupport reactionSummarySupport) {
      ChatTranscriptSenderStyleSupport.Context senderStyleSupportContext =
          new ChatTranscriptSenderStyleSupport.Context(
              styles,
              null,
              ChatTranscriptLineMetaSupport::bind,
              (fromStyle, messageStyle, outgoingLocalEcho) -> {},
              (fromStyle, messageStyle, rawNotificationColor) -> {});
      this.coordinator =
          new ChatTranscriptMessageInteractionCoordinator(
              docs,
              stateByTarget,
              this::ensureTargetAndMark,
              this::noteEpoch,
              messageCatalogSupport,
              reactionSummarySupport,
              senderStyleSupportContext,
              (ref, fromNick) -> fromNick,
              (ref, insertAt, from, action, outgoingLocalEcho, meta) -> {
                throw new AssertionError("Unexpected action replacement");
              },
              this::insertStandardLine,
              "[redacted]");
    }

    private void ensureTargetAndMark(TargetRef ref) {
      ensured.set(true);
      ensureTarget(ref);
    }

    private void ensureTarget(TargetRef ref) {
      docs.computeIfAbsent(ref, ignored -> new DefaultStyledDocument());
      stateByTarget.computeIfAbsent(
          ref,
          ignored ->
              new ChatTranscriptState(
                  messageCatalogSupport.createState(32, 32),
                  new ChatTranscriptFilteredLinesSupport.State(),
                  new ChatTranscriptPresenceFoldSupport.State()));
    }

    private void noteEpoch(TargetRef ref, Long epochMs) {
      ChatTranscriptState state = stateByTarget.get(ref);
      if (state == null || epochMs == null) {
        return;
      }
      state.noteEpochMs(epochMs);
    }

    private void insertMessageLine(TargetRef ref, String from, String text, String messageId)
        throws Exception {
      insertMessageLine(ref, from, text, messageId, LogDirection.IN, false);
    }

    private void insertMessageLine(
        TargetRef ref,
        String from,
        String text,
        String messageId,
        LogDirection direction,
        boolean outgoingAttribute)
        throws Exception {
      StyledDocument doc = document(ref);
      SimpleAttributeSet attrs =
          ChatTranscriptLineMetaSupport.bind(
              new SimpleAttributeSet(),
              ChatTranscriptLineMetaSupport.create(
                  ref, LogKind.CHAT, direction, from, 1_000L, null, messageId, Map.of()));
      if (outgoingAttribute) {
        attrs.addAttribute(ChatStyles.ATTR_OUTGOING, Boolean.TRUE);
      }
      doc.insertString(0, from + ": " + text + "\n", attrs);
    }

    private void insertStandardLine(
        TargetRef ref,
        int insertAt,
        String from,
        String text,
        javax.swing.text.AttributeSet fromStyle,
        javax.swing.text.AttributeSet messageStyle,
        cafe.woden.ircclient.ui.chat.transcript.line.LineMeta meta) {
      StyledDocument doc = document(ref);
      try {
        doc.insertString(
            insertAt,
            from + ": " + text + "\n",
            ChatTranscriptLineMetaSupport.bind(new SimpleAttributeSet(messageStyle), meta));
      } catch (Exception ex) {
        throw new RuntimeException(ex);
      }
    }

    private StyledDocument document(TargetRef ref) {
      return docs.get(ref);
    }

    private ChatTranscriptState state(TargetRef ref) {
      return stateByTarget.get(ref);
    }
  }
}
