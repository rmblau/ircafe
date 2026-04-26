package cafe.woden.ircclient.ui.chat.transcript;

import cafe.woden.ircclient.ui.chat.transcript.line.LineMeta;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import cafe.woden.ircclient.model.FilterAction;
import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.transcript.filter.ChatTranscriptFilterRoutingSupport;
import cafe.woden.ircclient.ui.chat.transcript.filter.ChatTranscriptFilteredLinesSupport;
import cafe.woden.ircclient.ui.chat.transcript.flow.ChatTranscriptChatFlowSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptLineMetaSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptOutgoingChatSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptPresenceFoldSupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageCatalogSupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageStateSupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptReactionSummarySupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptSenderStyleSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptState;
import cafe.woden.ircclient.ui.filter.FilterEngine;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.text.AttributeSet;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledDocument;
import org.junit.jupiter.api.Test;

class ChatTranscriptChatFlowSupportTest {

  @Test
  void appendChatAtRunsReplyContextAndPostAppendForLiveMessages() {
    ChatTranscriptReactionSummarySupport reactionSummarySupport =
        mock(ChatTranscriptReactionSummarySupport.class);
    Fixture fixture = new Fixture(reactionSummarySupport, newFilterRoutingSupport(null));

    fixture.support.appendChatAt(
        fixture.context,
        fixture.ref,
        "bob",
        "reply body",
        false,
        2_000L,
        "m-2",
        Map.of("msgid", "m-2", "draft/reply", "m-1", "draft/react", ":+1:"),
        null);

    assertEquals("reply body", fixture.appendCapture.text.get());
    assertTrue(fixture.appendCapture.allowEmbeds.get());
    assertEquals("m-1", fixture.replyContextTarget.get());
    verify(reactionSummarySupport)
        .materializePendingReactionsForMessage(
            fixture.ref, fixture.document(), fixture.state().reactionSummary(), "m-2", 2_000L);
    verify(reactionSummarySupport)
        .applyMessageReaction(
            fixture.ref,
            fixture.document(),
            fixture.state().reactionSummary(),
            "m-1",
            ":+1:",
            "bob",
            2_000L);
  }

  @Test
  void appendChatFromHistoryKeepsEmbedsDisabledAndSkipsReplyFollowUp() {
    ChatTranscriptReactionSummarySupport reactionSummarySupport =
        mock(ChatTranscriptReactionSummarySupport.class);
    Fixture fixture = new Fixture(reactionSummarySupport, newFilterRoutingSupport(null));

    fixture.support.appendChatFromHistory(
        fixture.context,
        fixture.ref,
        "bob",
        "historic reply",
        false,
        3_000L,
        "m-h2",
        Map.of("msgid", "m-h2", "draft/reply", "m-1", "draft/react", ":+1:"));

    assertEquals("historic reply", fixture.appendCapture.text.get());
    assertFalse(fixture.appendCapture.allowEmbeds.get());
    assertEquals(null, fixture.replyContextTarget.get());
    verifyNoInteractions(reactionSummarySupport);
  }

  @Test
  void appendChatAtSkipsDuplicateMessageIdsAlreadyPresentInTranscript() throws Exception {
    Fixture fixture =
        new Fixture(
            mock(ChatTranscriptReactionSummarySupport.class), newFilterRoutingSupport(null));
    LineMeta meta =
        ChatTranscriptLineMetaSupport.create(
            fixture.ref,
            cafe.woden.ircclient.model.LogKind.CHAT,
            cafe.woden.ircclient.model.LogDirection.IN,
            "alice",
            1_000L,
            null,
            "m-1",
            Map.of("msgid", "m-1"));
    SimpleAttributeSet attrs = ChatTranscriptLineMetaSupport.bind(new SimpleAttributeSet(), meta);
    fixture.document().insertString(0, "existing\n", attrs);

    fixture.support.appendChatAt(
        fixture.context,
        fixture.ref,
        "alice",
        "duplicate",
        false,
        1_500L,
        "m-1",
        Map.of("msgid", "m-1"),
        null);

    assertEquals(null, fixture.appendCapture.text.get());
  }

  @Test
  void insertChatFromHistoryAtReturnsHandledOffsetForHiddenMatches() {
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
        fixture.support.insertChatFromHistoryAt(
            fixture.context,
            fixture.ref,
            4,
            "alice",
            "hidden line",
            false,
            5_000L,
            "m-h1",
            Map.of("msgid", "m-h1"));

    assertEquals(11, nextInsertAt);
    assertEquals(1, hiddenInsertCalls.get());
    assertEquals(0, fixture.insertCapture.calls.get());
  }

  private static ChatTranscriptFilterRoutingSupport newFilterRoutingSupport(
      FilterEngine filterEngine) {
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
    private final ChatTranscriptChatFlowSupport support = new ChatTranscriptChatFlowSupport();
    private final TargetRef ref = new TargetRef("srv", "#chan");
    private final Map<TargetRef, StyledDocument> docs = new HashMap<>();
    private final Map<TargetRef, ChatTranscriptState> states = new HashMap<>();
    private final AppendCapture appendCapture = new AppendCapture();
    private final InsertCapture insertCapture = new InsertCapture();
    private final AtomicReference<String> replyContextTarget = new AtomicReference<>();
    private final AtomicInteger presenceBreaks = new AtomicInteger();
    private final ChatTranscriptChatFlowSupport.Context context;

    private Fixture(
        ChatTranscriptReactionSummarySupport reactionSummarySupport,
        ChatTranscriptFilterRoutingSupport filterRoutingSupport) {
      ChatTranscriptOutgoingChatSupport outgoingChatSupport =
          new ChatTranscriptOutgoingChatSupport(
              new ChatStyles(null),
              senderStyleContext(),
              this::ensureTargetExists,
              (target, epochMs) -> {},
              target -> presenceBreaks.incrementAndGet(),
              (target, from, text, fromStyle, msgStyle, meta, tailComponent, tailAttrs) -> {},
              (target, insertAt, from, text, fromStyle, msgStyle, meta) -> insertAt + 1,
              (target, after, messageStyle, meta) -> {});
      this.context =
          new ChatTranscriptChatFlowSupport.Context(
              filterRoutingSupport,
              senderStyleContext(),
              outgoingChatSupport,
              reactionSummarySupport,
              docs::get,
              states::get,
              this::ensureTargetExists,
              (target, epochMs) -> {},
              appendCapture,
              insertCapture,
              (target, fromNick, replyToMsgId, tsEpochMs) -> replyContextTarget.set(replyToMsgId),
              () -> true);
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
  }

  private static final class AppendCapture
      implements ChatTranscriptChatFlowSupport.AppendVisibleLineHandler {
    private final AtomicReference<String> text = new AtomicReference<>();
    private final AtomicReference<Boolean> allowEmbeds = new AtomicReference<>();

    @Override
    public void append(
        TargetRef ref,
        String from,
        String text,
        AttributeSet fromStyle,
        AttributeSet msgStyle,
        boolean allowEmbeds,
        LineMeta meta) {
      this.text.set(text);
      this.allowEmbeds.set(allowEmbeds);
    }
  }

  private static final class InsertCapture
      implements ChatTranscriptChatFlowSupport.InsertVisibleLineHandler {
    private final AtomicInteger calls = new AtomicInteger();

    @Override
    public int insert(
        TargetRef ref,
        int insertAt,
        String from,
        String text,
        AttributeSet fromStyle,
        AttributeSet msgStyle,
        LineMeta meta) {
      calls.incrementAndGet();
      return insertAt + 3;
    }
  }
}
