package cafe.woden.ircclient.ui.chat.transcript;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import cafe.woden.ircclient.model.FilterAction;
import cafe.woden.ircclient.model.LogDirection;
import cafe.woden.ircclient.model.LogKind;
import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.embed.ChatImageEmbedder;
import cafe.woden.ircclient.ui.chat.embed.ChatLinkPreviewEmbedder;
import cafe.woden.ircclient.ui.chat.render.ChatRichTextRenderer;
import cafe.woden.ircclient.ui.filter.FilterEngine;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledDocument;
import org.junit.jupiter.api.Test;

class ChatTranscriptActionFlowSupportTest {

  @Test
  void appendActionAtRunsReplyContextAndPostAppendForLiveMessages() throws Exception {
    ChatTranscriptReactionSummarySupport reactionSummarySupport =
        mock(ChatTranscriptReactionSummarySupport.class);
    Fixture fixture = new Fixture(reactionSummarySupport, newFilterRoutingSupport(null));

    fixture.support.appendActionAt(
        fixture.context,
        fixture.ref,
        "bob",
        "waves",
        false,
        2_000L,
        "m-2",
        Map.of("msgid", "m-2", "draft/reply", "m-1", "draft/react", ":+1:"),
        null);

    String transcript = fixture.documentText();
    assertTrue(transcript.contains("* bob waves"));
    assertEquals("m-1", fixture.replyContextTarget.get());
    verify(reactionSummarySupport)
        .materializePendingReactionsForMessage(
            fixture.ref, fixture.document(), fixture.state().reactionSummary, "m-2", 2_000L);
    verify(reactionSummarySupport)
        .applyMessageReaction(
            fixture.ref,
            fixture.document(),
            fixture.state().reactionSummary,
            "m-1",
            ":+1:",
            "bob",
            2_000L);
  }

  @Test
  void appendActionFromHistoryKeepsEmbedsDisabledAndSkipsReplyFollowUp() {
    ChatTranscriptReactionSummarySupport reactionSummarySupport =
        mock(ChatTranscriptReactionSummarySupport.class);
    Fixture fixture = new Fixture(reactionSummarySupport, newFilterRoutingSupport(null));

    fixture.support.appendActionFromHistory(
        fixture.context,
        fixture.ref,
        "bob",
        "historic wave",
        false,
        3_000L,
        "m-h2",
        Map.of("msgid", "m-h2", "draft/reply", "m-1", "draft/react", ":+1:"));

    assertTrue(fixture.documentText().contains("* bob historic wave"));
    assertEquals(null, fixture.replyContextTarget.get());
    verifyNoInteractions(reactionSummarySupport);
  }

  @Test
  void appendActionAtSkipsDuplicateMessageIdsAlreadyPresentInTranscript() throws Exception {
    Fixture fixture = new Fixture(mock(ChatTranscriptReactionSummarySupport.class), newFilterRoutingSupport(null));
    LineMeta meta =
        ChatTranscriptLineMetaSupport.create(
            fixture.ref,
            LogKind.ACTION,
            LogDirection.IN,
            "alice",
            1_000L,
            null,
            "m-1",
            Map.of("msgid", "m-1"));
    SimpleAttributeSet attrs = ChatTranscriptLineMetaSupport.bind(new SimpleAttributeSet(), meta);
    fixture.document().insertString(0, "existing\n", attrs);
    int before = fixture.document().getLength();

    fixture.support.appendActionAt(
        fixture.context,
        fixture.ref,
        "alice",
        "duplicate",
        false,
        1_500L,
        "m-1",
        Map.of("msgid", "m-1"),
        null);

    assertEquals(before, fixture.document().getLength());
    assertEquals(null, fixture.replyContextTarget.get());
  }

  @Test
  void insertActionFromHistoryAtReturnsHandledOffsetForHiddenMatches() {
    FilterEngine filterEngine = mock(FilterEngine.class);
    when(filterEngine.firstMatch(any()))
        .thenReturn(new FilterEngine.Match(UUID.randomUUID(), "hide", FilterAction.HIDE));
    AtomicInteger hiddenInsertCalls = new AtomicInteger();
    ChatTranscriptFilterRoutingSupport routingSupport =
        new ChatTranscriptFilterRoutingSupport(
            filterEngine,
            (ref, preview, meta, match) -> {},
            (ref, insertAt, preview, meta, match) -> {
              hiddenInsertCalls.incrementAndGet();
              return insertAt + 7;
            },
            ref -> {},
            ref -> {});
    Fixture fixture = new Fixture(mock(ChatTranscriptReactionSummarySupport.class), routingSupport);

    int nextInsertAt =
        fixture.support.insertActionFromHistoryAt(
            fixture.context,
            fixture.ref,
            4,
            "alice",
            "hidden action",
            false,
            5_000L,
            "m-h1",
            Map.of("msgid", "m-h1"));

    assertEquals(11, nextInsertAt);
    assertEquals(1, hiddenInsertCalls.get());
    assertEquals(0, fixture.filteredInsertRunBreaks.get());
  }

  @Test
  void insertActionFromHistoryAtBreaksFilteredInsertRunForVisibleLines() {
    Fixture fixture = new Fixture(mock(ChatTranscriptReactionSummarySupport.class), newFilterRoutingSupport(null));

    int nextInsertAt =
        fixture.support.insertActionFromHistoryAt(
            fixture.context,
            fixture.ref,
            0,
            "alice",
            "visible action",
            false,
            5_000L,
            "m-h1",
            Map.of("msgid", "m-h1"));

    assertTrue(nextInsertAt > 0);
    assertEquals(1, fixture.filteredInsertRunBreaks.get());
    assertTrue(fixture.documentText().contains("* alice visible action"));
  }

  private static ChatTranscriptFilterRoutingSupport newFilterRoutingSupport(FilterEngine filterEngine) {
    return new ChatTranscriptFilterRoutingSupport(
        filterEngine,
        (ref, preview, meta, match) -> {},
        (ref, insertAt, preview, meta, match) -> insertAt,
        ref -> {},
        ref -> {});
  }

  private static ChatTranscriptSenderStyleSupport.Context senderStyleContext() {
    ChatStyles styles = new ChatStyles(null);
    return new ChatTranscriptSenderStyleSupport.Context(
        styles,
        null,
        ChatTranscriptLineMetaSupport::bind,
        (fromStyle, messageStyle, outgoingLocalEcho) -> {
          if (outgoingLocalEcho) {
            fromStyle.addAttribute(ChatStyles.ATTR_OUTGOING, Boolean.TRUE);
            messageStyle.addAttribute(ChatStyles.ATTR_OUTGOING, Boolean.TRUE);
          }
        },
        (fromStyle, messageStyle, rawNotificationColor) -> {});
  }

  private static ChatTranscriptActionAppendSupport.Context actionAppendContext() {
    ChatStyles styles = new ChatStyles(null);
    ChatTranscriptMessageStateSupport.Context messageStateContext =
        new ChatTranscriptMessageStateSupport.Context(120, "[message redacted]", () -> 0L);
    return new ChatTranscriptActionAppendSupport.Context(
        styles,
        senderStyleContext(),
        new ChatTimestampFormatter(null, null),
        new ChatRichTextRenderer(null, null, styles, null),
        new ChatTranscriptManualPreviewSupport(
            styles, mock(ChatImageEmbedder.class), mock(ChatLinkPreviewEmbedder.class)),
        new ChatTranscriptMessageCatalogSupport(messageStateContext),
        (ref, from) -> from,
        (base, match) -> new SimpleAttributeSet(base),
        doc -> {},
        (ref, doc) -> 0,
        (ref, epochMs) -> {});
  }

  private static ChatTranscriptActionHistoryInsertSupport.Context actionHistoryInsertContext() {
    ChatStyles styles = new ChatStyles(null);
    ChatTranscriptMessageStateSupport.Context messageStateContext =
        new ChatTranscriptMessageStateSupport.Context(120, "[message redacted]", () -> 0L);
    return new ChatTranscriptActionHistoryInsertSupport.Context(
        styles,
        senderStyleContext(),
        new ChatTimestampFormatter(null, null),
        new ChatRichTextRenderer(null, null, styles, null),
        new ChatTranscriptMessageCatalogSupport(messageStateContext),
        (ref, from) -> from,
        (base, match) -> new SimpleAttributeSet(base),
        (doc, insertAt) -> insertAt,
        (doc, insertAt) -> insertAt,
        (ref, insertAt, delta) -> {},
        (ref, doc) -> 0,
        (ref, epochMs) -> {});
  }

  private static ChatTranscriptState newState() {
    ChatTranscriptMessageStateSupport.Context messageStateContext =
        new ChatTranscriptMessageStateSupport.Context(120, "[message redacted]", () -> 0L);
    ChatTranscriptMessageCatalogSupport messageCatalogSupport =
        new ChatTranscriptMessageCatalogSupport(messageStateContext);
    return new ChatTranscriptState(
        messageCatalogSupport.createState(16, 16),
        new ChatTranscriptFilteredLinesSupport.State(),
        new ChatTranscriptPresenceFoldSupport.State());
  }

  private static final class Fixture {
    private final ChatTranscriptActionFlowSupport support = new ChatTranscriptActionFlowSupport();
    private final TargetRef ref = new TargetRef("srv", "#chan");
    private final Map<TargetRef, StyledDocument> docs = new HashMap<>();
    private final Map<TargetRef, ChatTranscriptState> states = new HashMap<>();
    private final AtomicReference<String> replyContextTarget = new AtomicReference<>();
    private final AtomicInteger filteredInsertRunBreaks = new AtomicInteger();
    private final ChatTranscriptActionFlowSupport.Context context;

    private Fixture(
        ChatTranscriptReactionSummarySupport reactionSummarySupport,
        ChatTranscriptFilterRoutingSupport filterRoutingSupport) {
      this.context =
          new ChatTranscriptActionFlowSupport.Context(
              filterRoutingSupport,
              actionAppendContext(),
              actionHistoryInsertContext(),
              reactionSummarySupport,
              docs::get,
              states::get,
              this::ensureTargetExists,
              (target, epochMs) -> {},
              (target, fromNick, replyToMsgId, tsEpochMs) -> replyContextTarget.set(replyToMsgId),
              target -> filteredInsertRunBreaks.incrementAndGet(),
              target -> false,
              () -> true,
              () -> false,
              () -> false);
      ensureTargetExists(ref);
    }

    private void ensureTargetExists(TargetRef target) {
      docs.computeIfAbsent(target, ignored -> new DefaultStyledDocument());
      states.computeIfAbsent(target, ignored -> newState());
    }

    private StyledDocument document() {
      return docs.get(ref);
    }

    private ChatTranscriptState state() {
      return states.get(ref);
    }

    private String documentText() {
      try {
        return document().getText(0, document().getLength());
      } catch (Exception ignored) {
        return "";
      }
    }
  }
}
