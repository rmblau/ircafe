package cafe.woden.ircclient.ui.chat.transcript.line;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.model.LogDirection;
import cafe.woden.ircclient.model.LogKind;
import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.render.ChatRichTextRenderer;
import cafe.woden.ircclient.ui.chat.transcript.ChatTimestampFormatter;
import cafe.woden.ircclient.ui.chat.transcript.LineMeta;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageCatalogSupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageStateSupport;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import org.junit.jupiter.api.Test;

class ChatTranscriptTextInsertSupportTest {

  @Test
  void insertVisibleLineRecordsPreviewAndShiftsPresenceBlock() {
    ChatTranscriptTextInsertSupport.Context context = newContext();
    ChatTranscriptMessageCatalogSupport messageCatalogSupport = context.messageCatalogSupport();
    ChatTranscriptMessageCatalogSupport.State catalogState =
        messageCatalogSupport.createState(8, 8);
    AtomicInteger shiftedBy = new AtomicInteger();
    context =
        new ChatTranscriptTextInsertSupport.Context(
            context.styles(),
            context.timestamps(),
            context.renderer(),
            messageCatalogSupport,
            context.renderedFromResolver(),
            context.filterMatchStyler(),
            context.insertAtNormalizer(),
            context.insertLineStartEnsurer(),
            (ref, insertAt, delta) -> shiftedBy.set(delta),
            context.transcriptLineCapEnforcer());
    DefaultStyledDocument doc = new DefaultStyledDocument();

    int nextOffset =
        ChatTranscriptTextInsertSupport.insertVisibleLine(
            context,
            new TargetRef("srv", "#chan"),
            doc,
            catalogState,
            0,
            "alice",
            "hello",
            null,
            null,
            lineMeta(),
            null,
            true,
            false,
            false);

    assertTrue(text(doc).contains("alice: hello"));
    assertEquals("alice: hello", messageCatalogSupport.previewForMessageId(catalogState, "m-1"));
    assertTrue(nextOffset > 0);
    assertTrue(shiftedBy.get() > 0);
  }

  @Test
  void insertVisibleLineAdjustsReturnOffsetWhenLineCapTrimmed() {
    ChatTranscriptTextInsertSupport.Context base = newContext();
    ChatTranscriptTextInsertSupport.Context context =
        new ChatTranscriptTextInsertSupport.Context(
            base.styles(),
            base.timestamps(),
            base.renderer(),
            base.messageCatalogSupport(),
            base.renderedFromResolver(),
            base.filterMatchStyler(),
            base.insertAtNormalizer(),
            base.insertLineStartEnsurer(),
            base.presenceBlockShifter(),
            (ref, doc) -> 2);
    DefaultStyledDocument doc = new DefaultStyledDocument();

    int nextOffset =
        ChatTranscriptTextInsertSupport.insertVisibleLine(
            context,
            new TargetRef("srv", "#chan"),
            doc,
            null,
            0,
            "alice",
            "hello",
            null,
            null,
            lineMeta(),
            null,
            true,
            false,
            false);

    assertEquals(Math.max(0, doc.getLength() - 2), nextOffset);
  }

  private static ChatTranscriptTextInsertSupport.Context newContext() {
    ChatStyles styles = new ChatStyles(null);
    ChatTranscriptMessageStateSupport.Context messageStateContext =
        new ChatTranscriptMessageStateSupport.Context(120, "[message redacted]", () -> 0L);
    return new ChatTranscriptTextInsertSupport.Context(
        styles,
        new ChatTimestampFormatter(null, null),
        new ChatRichTextRenderer(null, null, styles, null),
        new ChatTranscriptMessageCatalogSupport(messageStateContext),
        (ref, from) -> from,
        (base, match) -> new SimpleAttributeSet(base),
        (doc, insertAt) -> insertAt,
        (doc, insertAt) -> insertAt,
        (ref, insertAt, delta) -> {},
        (ref, doc) -> 0);
  }

  private static LineMeta lineMeta() {
    return new LineMeta(
        "srv/#chan", LogKind.CHAT, LogDirection.IN, "alice", 1_000L, Set.of(), "m-1", "", Map.of());
  }

  private static String text(DefaultStyledDocument doc) {
    try {
      return doc.getText(0, doc.getLength());
    } catch (Exception ignored) {
      return "";
    }
  }
}
