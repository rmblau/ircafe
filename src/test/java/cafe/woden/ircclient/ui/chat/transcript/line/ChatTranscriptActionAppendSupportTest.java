package cafe.woden.ircclient.ui.chat.transcript.line;

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
import cafe.woden.ircclient.ui.chat.transcript.ChatTimestampFormatter;
import cafe.woden.ircclient.ui.chat.transcript.LineMeta;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageCatalogSupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageStateSupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptSenderStyleSupport;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import org.junit.jupiter.api.Test;

class ChatTranscriptActionAppendSupportTest {

  @Test
  void appendVisibleActionWritesActionLineAndUpdatesReadMarker() throws Exception {
    ChatTranscriptActionAppendSupport.Context context = newContext(null, null);
    DefaultStyledDocument doc = new DefaultStyledDocument();
    AtomicInteger readMarkerCalls = new AtomicInteger();
    context =
        new ChatTranscriptActionAppendSupport.Context(
            context.styles(),
            context.senderStyleSupportContext(),
            context.timestamps(),
            context.renderer(),
            context.manualPreviewSupport(),
            context.messageCatalogSupport(),
            context.renderedFromResolver(),
            context.filterMatchStyler(),
            context.ensureAtLineStart(),
            context.transcriptLineCapEnforcer(),
            (ref, epochMs) -> readMarkerCalls.incrementAndGet());

    ChatTranscriptActionAppendSupport.appendVisibleAction(
        context,
        new TargetRef("srv", "#chan"),
        doc,
        null,
        "alice",
        "waves",
        false,
        false,
        1_000L,
        null,
        Map.of(),
        lineMeta(),
        null,
        true,
        false,
        false);

    assertTrue(doc.getText(0, doc.getLength()).contains("* alice waves"));
    assertEquals(1, readMarkerCalls.get());
  }

  @Test
  void appendVisibleActionAddsBlockedManualPreviewMarkerWhenEmbedsBlocked() throws Exception {
    ChatImageEmbedder imageEmbeds = mock(ChatImageEmbedder.class);
    ChatLinkPreviewEmbedder linkPreviews = mock(ChatLinkPreviewEmbedder.class);
    when(imageEmbeds.appendEmbeds(any(), any(), anyString(), anyString(), any()))
        .thenReturn(new ChatImageEmbedder.AppendResult(0, List.of("https://blocked.example/a")));
    when(linkPreviews.appendPreviews(any(), any(), anyString(), anyString(), any()))
        .thenReturn(new ChatLinkPreviewEmbedder.AppendResult(0, List.of()));
    ChatTranscriptActionAppendSupport.Context context = newContext(imageEmbeds, linkPreviews);
    DefaultStyledDocument doc = new DefaultStyledDocument();

    ChatTranscriptActionAppendSupport.appendVisibleAction(
        context,
        new TargetRef("srv", "#chan"),
        doc,
        null,
        "alice",
        "https://blocked.example/a",
        false,
        true,
        1_000L,
        null,
        Map.of(),
        lineMeta(),
        null,
        true,
        true,
        true);

    int marker = doc.getText(0, doc.getLength()).indexOf("👁");
    assertTrue(marker >= 0);
    assertEquals(
        "https://blocked.example/a",
        doc.getCharacterElement(marker)
            .getAttributes()
            .getAttribute(ChatStyles.ATTR_MANUAL_PREVIEW_URL));
  }

  private static ChatTranscriptActionAppendSupport.Context newContext(
      ChatImageEmbedder imageEmbeds, ChatLinkPreviewEmbedder linkPreviews) {
    ChatStyles styles = new ChatStyles(null);
    ChatTranscriptMessageStateSupport.Context messageStateContext =
        new ChatTranscriptMessageStateSupport.Context(120, "[message redacted]", () -> 0L);
    return new ChatTranscriptActionAppendSupport.Context(
        styles,
        new ChatTranscriptSenderStyleSupport.Context(
            styles,
            null,
            ChatTranscriptLineMetaSupport::bind,
            (fromStyle, messageStyle, outgoingLocalEcho) -> {},
            (fromStyle, messageStyle, rawNotificationColor) -> {}),
        new ChatTimestampFormatter(null, null),
        new ChatRichTextRenderer(null, null, styles, null),
        new ChatTranscriptManualPreviewSupport(styles, imageEmbeds, linkPreviews),
        new ChatTranscriptMessageCatalogSupport(messageStateContext),
        (ref, from) -> from,
        (base, match) -> new SimpleAttributeSet(base),
        doc -> {},
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
}
