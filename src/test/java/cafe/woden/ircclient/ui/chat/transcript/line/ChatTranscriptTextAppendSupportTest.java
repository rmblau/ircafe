package cafe.woden.ircclient.ui.chat.transcript;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cafe.woden.ircclient.model.LogDirection;
import cafe.woden.ircclient.model.LogKind;
import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.embed.ChatImageEmbedder;
import cafe.woden.ircclient.ui.chat.embed.ChatLinkPreviewEmbedder;
import cafe.woden.ircclient.ui.chat.render.ChatRichTextRenderer;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import org.junit.jupiter.api.Test;

class ChatTranscriptTextAppendSupportTest {

  @Test
  void appendVisibleLineRecordsPreviewAndUpdatesReadMarker() {
    ChatTranscriptTextAppendSupport.Context context = newContext(null, null);
    ChatTranscriptMessageCatalogSupport messageCatalogSupport = context.messageCatalogSupport();
    ChatTranscriptMessageCatalogSupport.State catalogState =
        messageCatalogSupport.createState(8, 8);
    AtomicInteger readMarkerCalls = new AtomicInteger();
    context =
        new ChatTranscriptTextAppendSupport.Context(
            context.styles(),
            context.timestamps(),
            context.renderer(),
            messageCatalogSupport,
            context.manualPreviewSupport(),
            context.renderedFromResolver(),
            context.filterMatchStyler(),
            context.transcriptLineCapEnforcer(),
            (ref, epochMs) -> readMarkerCalls.incrementAndGet());
    DefaultStyledDocument doc = new DefaultStyledDocument();

    ChatTranscriptTextAppendSupport.appendVisibleLine(
        context,
        new TargetRef("srv", "#chan"),
        doc,
        catalogState,
        "alice",
        "hello",
        null,
        null,
        false,
        lineMeta(),
        null,
        null,
        null,
        true,
        false,
        false,
        false,
        false);

    assertTrue(text(doc).contains("alice: hello"));
    assertEquals("alice: hello", messageCatalogSupport.previewForMessageId(catalogState, "m-1"));
    assertEquals(1, readMarkerCalls.get());
  }

  @Test
  void appendVisibleLineAddsBlockedManualPreviewMarkerWhenEmbedsBlocked() {
    ChatImageEmbedder imageEmbeds = mock(ChatImageEmbedder.class);
    ChatLinkPreviewEmbedder linkPreviews = mock(ChatLinkPreviewEmbedder.class);
    when(imageEmbeds.appendEmbeds(any(), any(), anyString(), anyString(), any()))
        .thenReturn(new ChatImageEmbedder.AppendResult(0, List.of("https://blocked.example/a")));
    when(linkPreviews.appendPreviews(any(), any(), anyString(), anyString(), any()))
        .thenReturn(new ChatLinkPreviewEmbedder.AppendResult(0, List.of()));
    ChatTranscriptTextAppendSupport.Context context = newContext(imageEmbeds, linkPreviews);
    DefaultStyledDocument doc = new DefaultStyledDocument();

    ChatTranscriptTextAppendSupport.appendVisibleLine(
        context,
        new TargetRef("srv", "#chan"),
        doc,
        null,
        "alice",
        "https://blocked.example/a",
        null,
        null,
        true,
        lineMeta(),
        null,
        null,
        null,
        true,
        false,
        false,
        true,
        true);

    int marker = text(doc).indexOf("👁");
    assertTrue(marker >= 0);
    assertEquals(
        "https://blocked.example/a",
        doc.getCharacterElement(marker)
            .getAttributes()
            .getAttribute(ChatStyles.ATTR_MANUAL_PREVIEW_URL));
  }

  private static ChatTranscriptTextAppendSupport.Context newContext(
      ChatImageEmbedder imageEmbeds, ChatLinkPreviewEmbedder linkPreviews) {
    ChatStyles styles = new ChatStyles(null);
    ChatTranscriptMessageStateSupport.Context messageStateContext =
        new ChatTranscriptMessageStateSupport.Context(120, "[message redacted]", () -> 0L);
    return new ChatTranscriptTextAppendSupport.Context(
        styles,
        new ChatTimestampFormatter(null, null),
        new ChatRichTextRenderer(null, null, styles, null),
        new ChatTranscriptMessageCatalogSupport(messageStateContext),
        new ChatTranscriptManualPreviewSupport(styles, imageEmbeds, linkPreviews),
        (ref, from) -> from,
        (base, match) -> new SimpleAttributeSet(base),
        (ref, doc) -> 0,
        (ref, epochMs) -> {});
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
