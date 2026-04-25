package cafe.woden.ircclient.ui.chat.transcript;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cafe.woden.ircclient.model.FilterAction;
import cafe.woden.ircclient.model.LogDirection;
import cafe.woden.ircclient.model.LogKind;
import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.transcript.filter.ChatTranscriptFilterRoutingSupport;
import cafe.woden.ircclient.ui.chat.transcript.filter.ChatTranscriptFilteredLinesSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptLineMetaSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptPresenceFoldSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptSystemLineSupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageCatalogSupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageStateSupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptReactionSummarySupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptState;
import cafe.woden.ircclient.ui.filter.FilterEngine;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.text.AttributeSet;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledDocument;
import org.junit.jupiter.api.Test;

class ChatTranscriptSystemLineSupportTest {

  @Test
  void insertNoticeFromHistoryAtDelegatesVisibleInsertWithNoticeMetadata() {
    Fixture fixture = new Fixture(newFilterRoutingSupport(null));

    int nextInsertAt =
        fixture.support.insertNoticeFromHistoryAt(
            fixture.ref, 3, "nick", "historic notice", 2_000L, "m-2", Map.of("msgid", "m-2"));

    assertEquals(8, nextInsertAt);
    assertEquals(1, fixture.ensureCalls.get());
    assertEquals(2_000L, fixture.lastEpoch.get());
    assertEquals(fixture.ref, fixture.insertRef.get());
    assertEquals(3, fixture.insertAt.get());
    assertEquals("nick", fixture.insertFrom.get());
    assertEquals("historic notice", fixture.insertText.get());
    assertNotNull(fixture.insertMeta.get());
    assertEquals(LogKind.NOTICE, fixture.insertMeta.get().kind());
    assertEquals(LogDirection.IN, fixture.insertMeta.get().direction());
    assertEquals("m-2", fixture.insertMeta.get().messageId());
    assertFalse(fixture.insertedFromStyle.get().isEqual(fixture.styles.status()));
  }

  @Test
  void insertStatusFromHistoryAtReturnsHiddenOffsetWhenFilterHidesLine() {
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
              return insertAt + 9;
            },
            ref -> {},
            ref -> {});
    Fixture fixture = new Fixture(routingSupport);

    int nextInsertAt =
        fixture.support.insertStatusFromHistoryAt(
            fixture.ref, 4, "system", "hidden status", 5_000L);

    assertEquals(13, nextInsertAt);
    assertEquals(1, hiddenInsertCalls.get());
    assertFalse(fixture.insertCalled.get());
  }

  @Test
  void insertNoticeFromHistoryAtSkipsDuplicateMessageIdsAlreadyPresentInTranscript()
      throws Exception {
    Fixture fixture = new Fixture(newFilterRoutingSupport(null));
    LineMeta meta =
        ChatTranscriptLineMetaSupport.create(
            fixture.ref,
            LogKind.NOTICE,
            LogDirection.IN,
            "nick",
            1_000L,
            null,
            "m-1",
            Map.of("msgid", "m-1"));
    SimpleAttributeSet attrs = ChatTranscriptLineMetaSupport.bind(new SimpleAttributeSet(), meta);
    fixture.document.insertString(0, "existing\n", attrs);

    int nextInsertAt =
        fixture.support.insertNoticeFromHistoryAt(
            fixture.ref, 2, "nick", "duplicate", 2_000L, "m-1", Map.of("msgid", "m-1"));

    assertEquals(2, nextInsertAt);
    assertFalse(fixture.insertCalled.get());
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

  private static final class Fixture {
    final ChatStyles styles = new ChatStyles(null);
    final TargetRef ref = new TargetRef("server", "#chan");
    final StyledDocument document = new DefaultStyledDocument();
    final ChatTranscriptState state;
    final AtomicInteger ensureCalls = new AtomicInteger();
    final AtomicReference<Long> lastEpoch = new AtomicReference<>();
    final AtomicBoolean insertCalled = new AtomicBoolean();
    final AtomicReference<TargetRef> insertRef = new AtomicReference<>();
    final AtomicInteger insertAt = new AtomicInteger(-1);
    final AtomicReference<String> insertFrom = new AtomicReference<>();
    final AtomicReference<String> insertText = new AtomicReference<>();
    final AtomicReference<AttributeSet> insertedFromStyle = new AtomicReference<>();
    final AtomicReference<AttributeSet> insertedMessageStyle = new AtomicReference<>();
    final AtomicReference<LineMeta> insertMeta = new AtomicReference<>();

    final ChatTranscriptSystemLineSupport support;

    Fixture(ChatTranscriptFilterRoutingSupport filterRoutingSupport) {
      ChatTranscriptMessageStateSupport.Context messageStateContext =
          new ChatTranscriptMessageStateSupport.Context(120, "[message redacted]", () -> 0L);
      ChatTranscriptMessageCatalogSupport messageCatalogSupport =
          new ChatTranscriptMessageCatalogSupport(messageStateContext);
      state =
          new ChatTranscriptState(
              messageCatalogSupport.createState(32, 32),
              new ChatTranscriptFilteredLinesSupport.State(),
              new ChatTranscriptPresenceFoldSupport.State());
      support =
          new ChatTranscriptSystemLineSupport(
              filterRoutingSupport,
              ref -> ensureCalls.incrementAndGet(),
              (ref, epochMs) -> lastEpoch.set(epochMs),
              (ref, from, text, fromStyle, msgStyle, allowEmbeds, meta) -> {},
              (ref, insertAt, from, text, fromStyle, msgStyle, meta) -> {
                insertCalled.set(true);
                insertRef.set(ref);
                this.insertAt.set(insertAt);
                insertFrom.set(from);
                insertText.set(text);
                insertedFromStyle.set(fromStyle);
                insertedMessageStyle.set(msgStyle);
                insertMeta.set(meta);
                return insertAt + 5;
              },
              (ref, from, replyToMsgId, tsEpochMs) -> {},
              ref -> document,
              ref -> state,
              mock(ChatTranscriptReactionSummarySupport.class),
              ref ->
                  new ChatTranscriptSystemLineSupport.LineStyles(
                      styles.noticeFrom(), styles.noticeMessage()),
              ref ->
                  new ChatTranscriptSystemLineSupport.LineStyles(styles.status(), styles.status()),
              ref -> new ChatTranscriptSystemLineSupport.LineStyles(styles.error(), styles.error()),
              () -> 123L);
    }
  }
}
