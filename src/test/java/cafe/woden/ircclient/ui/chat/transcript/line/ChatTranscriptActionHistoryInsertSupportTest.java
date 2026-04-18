package cafe.woden.ircclient.ui.chat.transcript;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.model.LogDirection;
import cafe.woden.ircclient.model.LogKind;
import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.render.ChatRichTextRenderer;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import org.junit.jupiter.api.Test;

class ChatTranscriptActionHistoryInsertSupportTest {

  @Test
  void insertVisibleActionRecordsPreviewAndUpdatesReadMarker() {
    ChatTranscriptActionHistoryInsertSupport.Context context = newContext();
    ChatTranscriptMessageCatalogSupport messageCatalogSupport = context.messageCatalogSupport();
    ChatTranscriptMessageCatalogSupport.State catalogState =
        messageCatalogSupport.createState(8, 8);
    AtomicInteger readMarkerCalls = new AtomicInteger();
    AtomicInteger shiftedBy = new AtomicInteger();
    context =
        new ChatTranscriptActionHistoryInsertSupport.Context(
            context.styles(),
            context.senderStyleSupportContext(),
            context.timestamps(),
            context.renderer(),
            messageCatalogSupport,
            context.renderedFromResolver(),
            context.filterMatchStyler(),
            context.insertAtNormalizer(),
            context.insertLineStartEnsurer(),
            (ref, insertAt, delta) -> shiftedBy.set(delta),
            context.transcriptLineCapEnforcer(),
            (ref, epochMs) -> readMarkerCalls.incrementAndGet());
    DefaultStyledDocument doc = new DefaultStyledDocument();

    int nextOffset =
        ChatTranscriptActionHistoryInsertSupport.insertVisibleAction(
            context,
            new TargetRef("srv", "#chan"),
            doc,
            catalogState,
            0,
            "alice",
            "waves",
            false,
            1_000L,
            lineMeta(),
            null,
            true,
            false);

    assertTrue(text(doc).contains("* alice waves"));
    assertEquals("* alice waves", messageCatalogSupport.previewForMessageId(catalogState, "m-1"));
    assertTrue(nextOffset > 0);
    assertTrue(shiftedBy.get() > 0);
    assertEquals(1, readMarkerCalls.get());
  }

  @Test
  void insertVisibleActionCanSkipLineCapAndReadMarkerForReplacementFlow() {
    ChatTranscriptActionHistoryInsertSupport.Context base = newContext();
    AtomicInteger readMarkerCalls = new AtomicInteger();
    ChatTranscriptActionHistoryInsertSupport.Context context =
        new ChatTranscriptActionHistoryInsertSupport.Context(
            base.styles(),
            base.senderStyleSupportContext(),
            base.timestamps(),
            base.renderer(),
            base.messageCatalogSupport(),
            base.renderedFromResolver(),
            base.filterMatchStyler(),
            base.insertAtNormalizer(),
            base.insertLineStartEnsurer(),
            base.presenceBlockShifter(),
            (ref, doc) -> 2,
            (ref, epochMs) -> readMarkerCalls.incrementAndGet());
    DefaultStyledDocument doc = new DefaultStyledDocument();

    int nextOffset =
        ChatTranscriptActionHistoryInsertSupport.insertVisibleAction(
            context,
            new TargetRef("srv", "#chan"),
            doc,
            null,
            0,
            "alice",
            "waves",
            false,
            1_000L,
            lineMeta(),
            null,
            true,
            false,
            false,
            false);

    assertEquals(doc.getLength(), nextOffset);
    assertEquals(0, readMarkerCalls.get());
  }

  private static ChatTranscriptActionHistoryInsertSupport.Context newContext() {
    ChatStyles styles = new ChatStyles(null);
    ChatTranscriptMessageStateSupport.Context messageStateContext =
        new ChatTranscriptMessageStateSupport.Context(120, "[message redacted]", () -> 0L);
    return new ChatTranscriptActionHistoryInsertSupport.Context(
        styles,
        new ChatTranscriptSenderStyleSupport.Context(
            styles,
            null,
            ChatTranscriptLineMetaSupport::bind,
            (fromStyle, messageStyle, outgoingLocalEcho) -> {},
            (fromStyle, messageStyle, rawNotificationColor) -> {}),
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

  private static LineMeta lineMeta() {
    return new LineMeta(
        "srv/#chan",
        LogKind.ACTION,
        LogDirection.IN,
        "alice",
        1_000L,
        Set.of(),
        "m-1",
        "",
        Map.of());
  }

  private static String text(DefaultStyledDocument doc) {
    try {
      return doc.getText(0, doc.getLength());
    } catch (Exception ignored) {
      return "";
    }
  }
}
