package cafe.woden.ircclient.ui.chat.transcript.line;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.transcript.line.LineMeta;
import cafe.woden.ircclient.ui.chat.transcript.OutgoingSendIndicator;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptReactionSummarySupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptSenderStyleSupport;
import java.awt.Component;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledDocument;
import org.junit.jupiter.api.Test;

class ChatTranscriptOutgoingChatSupportTest {

  @Test
  void appendPendingOutgoingChatAddsSpinnerTailWhenIndicatorsEnabled() {
    CapturingAppendHandler appendHandler = new CapturingAppendHandler();
    ChatTranscriptOutgoingChatSupport support =
        newSupport(appendHandler, new CapturingInsertHandler());

    support.appendPendingOutgoingChat(
        new TargetRef("srv", "#chan"), " pending-1 ", "me", "hello", 10_000L, true);

    assertEquals("hello", appendHandler.text.get());
    assertInstanceOf(OutgoingSendIndicator.PendingSpinner.class, appendHandler.tailComponent.get());
    assertEquals(
        "pending-1", appendHandler.fromStyle.get().getAttribute(ChatStyles.ATTR_META_PENDING_ID));
    assertEquals(
        "pending-1",
        appendHandler.messageStyle.get().getAttribute(ChatStyles.ATTR_META_PENDING_ID));
  }

  @Test
  void appendPendingOutgoingChatSkipsSpinnerTailWhenIndicatorsDisabled() {
    CapturingAppendHandler appendHandler = new CapturingAppendHandler();
    ChatTranscriptOutgoingChatSupport support =
        newSupport(appendHandler, new CapturingInsertHandler());

    support.appendPendingOutgoingChat(
        new TargetRef("srv", "#chan"), "pending-2", "me", "hello", 10_000L, false);

    assertEquals("hello", appendHandler.text.get());
    assertEquals(null, appendHandler.tailComponent.get());
    assertEquals(null, appendHandler.tailAttrs.get());
  }

  @Test
  void insertCanonicalOutgoingChatLineAtAddsConfirmedDotAndRunsFollowUp() {
    CapturingAppendHandler appendHandler = new CapturingAppendHandler();
    CapturingInsertHandler insertHandler = new CapturingInsertHandler();
    AtomicReference<SimpleAttributeSet> confirmedDotStyle = new AtomicReference<>();
    AtomicReference<LineMeta> confirmedDotMeta = new AtomicReference<>();
    ChatTranscriptOutgoingChatSupport support =
        new ChatTranscriptOutgoingChatSupport(
            new ChatStyles(null),
            senderStyleContext(),
            ref -> {},
            (ref, epochMs) -> {},
            ref -> {},
            appendHandler,
            insertHandler,
            (ref, after, messageStyle, meta) -> {
              insertHandler.after.set(after);
              confirmedDotStyle.set(messageStyle);
              confirmedDotMeta.set(meta);
            });
    ChatTranscriptReactionSummarySupport reactionSummarySupport =
        mock(ChatTranscriptReactionSummarySupport.class);
    ChatTranscriptReactionSummarySupport.State reactionState =
        new ChatTranscriptReactionSummarySupport.State();
    StyledDocument doc = new javax.swing.text.DefaultStyledDocument();
    TargetRef ref = new TargetRef("srv", "#chan");

    support.insertCanonicalOutgoingChatLineAt(
        ref,
        doc,
        reactionSummarySupport,
        reactionState,
        4,
        "alice",
        "hello",
        11_000L,
        "m-1",
        Map.of("draft/reply", "m-0", "draft/react", ":+1:"),
        true);

    assertEquals("hello", insertHandler.text.get());
    assertEquals(9, insertHandler.after.get());
    assertNotNull(confirmedDotStyle.get());
    assertEquals("m-1", confirmedDotMeta.get().messageId());
    verify(reactionSummarySupport)
        .materializePendingReactionsForMessage(ref, doc, reactionState, "m-1", 11_000L);
    verify(reactionSummarySupport)
        .applyMessageReaction(ref, doc, reactionState, "m-0", ":+1:", "alice", 11_000L);
  }

  @Test
  void insertFailedOutgoingChatLineAtAppendsFailureSuffixAndOutgoingAttrs() {
    CapturingInsertHandler insertHandler = new CapturingInsertHandler();
    ChatTranscriptOutgoingChatSupport support =
        newSupport(new CapturingAppendHandler(), insertHandler);

    support.insertFailedOutgoingChatLineAt(
        new TargetRef("srv", "#chan"), 4, "me", "hello", 12_000L, "network");

    assertTrue(insertHandler.text.get().contains("hello [failed: network]"));
    assertEquals(
        Boolean.TRUE, insertHandler.fromStyle.get().getAttribute(ChatStyles.ATTR_OUTGOING));
    assertEquals(
        Boolean.TRUE, insertHandler.messageStyle.get().getAttribute(ChatStyles.ATTR_OUTGOING));
    assertEquals(
        ChatStyles.STYLE_ERROR,
        insertHandler.messageStyle.get().getAttribute(ChatStyles.ATTR_STYLE));
  }

  @Test
  void insertCanonicalOutgoingChatLineAtSkipsConfirmedDotWhenIndicatorsDisabled() {
    CapturingInsertHandler insertHandler = new CapturingInsertHandler();
    AtomicInteger confirmedDots = new AtomicInteger();
    ChatTranscriptOutgoingChatSupport support =
        new ChatTranscriptOutgoingChatSupport(
            new ChatStyles(null),
            senderStyleContext(),
            ref -> {},
            (ref, epochMs) -> {},
            ref -> {},
            new CapturingAppendHandler(),
            insertHandler,
            (ref, after, messageStyle, meta) -> confirmedDots.incrementAndGet());

    support.insertCanonicalOutgoingChatLineAt(
        new TargetRef("srv", "#chan"),
        new javax.swing.text.DefaultStyledDocument(),
        mock(ChatTranscriptReactionSummarySupport.class),
        new ChatTranscriptReactionSummarySupport.State(),
        4,
        "alice",
        "hello",
        11_000L,
        "m-1",
        Map.of(),
        false);

    assertEquals(0, confirmedDots.get());
  }

  private static ChatTranscriptOutgoingChatSupport newSupport(
      ChatTranscriptOutgoingChatSupport.AppendLineHandler appendHandler,
      ChatTranscriptOutgoingChatSupport.InsertLineHandler insertHandler) {
    return new ChatTranscriptOutgoingChatSupport(
        new ChatStyles(null),
        senderStyleContext(),
        ref -> {},
        (ref, epochMs) -> {},
        ref -> {},
        appendHandler,
        insertHandler,
        (ref, after, messageStyle, meta) -> {});
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

  private static final class CapturingAppendHandler
      implements ChatTranscriptOutgoingChatSupport.AppendLineHandler {
    private final AtomicReference<String> text = new AtomicReference<>();
    private final AtomicReference<SimpleAttributeSet> fromStyle = new AtomicReference<>();
    private final AtomicReference<SimpleAttributeSet> messageStyle = new AtomicReference<>();
    private final AtomicReference<Component> tailComponent = new AtomicReference<>();
    private final AtomicReference<AttributeSet> tailAttrs = new AtomicReference<>();

    @Override
    public void append(
        TargetRef ref,
        String from,
        String text,
        AttributeSet fromStyle,
        AttributeSet msgStyle,
        LineMeta meta,
        Component tailComponent,
        AttributeSet tailAttrs) {
      this.text.set(text);
      this.fromStyle.set(new SimpleAttributeSet(fromStyle));
      this.messageStyle.set(new SimpleAttributeSet(msgStyle));
      this.tailComponent.set(tailComponent);
      this.tailAttrs.set(tailAttrs);
    }
  }

  private static final class CapturingInsertHandler
      implements ChatTranscriptOutgoingChatSupport.InsertLineHandler {
    private final AtomicReference<String> text = new AtomicReference<>();
    private final AtomicReference<SimpleAttributeSet> fromStyle = new AtomicReference<>();
    private final AtomicReference<SimpleAttributeSet> messageStyle = new AtomicReference<>();
    private final AtomicInteger after = new AtomicInteger();

    @Override
    public int insert(
        TargetRef ref,
        int insertAt,
        String from,
        String text,
        AttributeSet fromStyle,
        AttributeSet msgStyle,
        LineMeta meta) {
      this.text.set(text);
      this.fromStyle.set(new SimpleAttributeSet(fromStyle));
      this.messageStyle.set(new SimpleAttributeSet(msgStyle));
      int result = insertAt + 5;
      after.set(result);
      return result;
    }
  }
}
