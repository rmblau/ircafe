package cafe.woden.ircclient.ui.chat.transcript.line;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cafe.woden.ircclient.model.LogDirection;
import cafe.woden.ircclient.model.LogKind;
import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.embed.ChatImageEmbedder;
import cafe.woden.ircclient.ui.chat.embed.ChatLinkPreviewEmbedder;
import cafe.woden.ircclient.ui.chat.transcript.line.LineMeta;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import org.junit.jupiter.api.Test;

class ChatTranscriptManualPreviewSupportTest {

  @Test
  void collectBlockedPreviewUrlsForAppendMergesImageAndLinkBlockedUrls() {
    ChatImageEmbedder imageEmbeds = mock(ChatImageEmbedder.class);
    ChatLinkPreviewEmbedder linkPreviews = mock(ChatLinkPreviewEmbedder.class);
    ChatTranscriptManualPreviewSupport support =
        new ChatTranscriptManualPreviewSupport(new ChatStyles(null), imageEmbeds, linkPreviews);
    DefaultStyledDocument doc = new DefaultStyledDocument();
    TargetRef ref = new TargetRef("srv", "#chan");

    when(imageEmbeds.appendEmbeds(any(), any(), anyString(), anyString(), any()))
        .thenReturn(
            new ChatImageEmbedder.AppendResult(
                0, List.of("https://blocked.example/a", "https://blocked.example/b")));
    when(linkPreviews.appendPreviews(any(), any(), anyString(), anyString(), any()))
        .thenReturn(
            new ChatLinkPreviewEmbedder.AppendResult(
                0, List.of("https://blocked.example/b", "https://blocked.example/c")));

    assertEquals(
        List.of(
            "https://blocked.example/a", "https://blocked.example/b", "https://blocked.example/c"),
        support.collectBlockedPreviewUrlsForAppend(
            ref, doc, "https://example", "alice", Map.of(), true, true));
  }

  @Test
  void insertManualPreviewMarkersNormalizesAndDeduplicatesUrls() throws Exception {
    ChatTranscriptManualPreviewSupport support =
        new ChatTranscriptManualPreviewSupport(new ChatStyles(null), null, null);
    DefaultStyledDocument doc = new DefaultStyledDocument();

    support.insertManualPreviewMarkers(
        doc,
        0,
        lineMeta(),
        null,
        List.of(" https://example.com/a ", "https://example.com/a", "   "),
        (attrs, match) -> new SimpleAttributeSet(attrs));

    String text = doc.getText(0, doc.getLength());
    int marker = text.indexOf("👁");
    assertTrue(marker >= 0);
    assertEquals(-1, text.indexOf("👁", marker + 1));
    assertEquals(
        "https://example.com/a",
        doc.getCharacterElement(marker)
            .getAttributes()
            .getAttribute(ChatStyles.ATTR_MANUAL_PREVIEW_URL));
  }

  @Test
  void appendBlockedPreviewMarkersForAppendCollectsAndInsertsMarkers() throws Exception {
    ChatImageEmbedder imageEmbeds = mock(ChatImageEmbedder.class);
    ChatLinkPreviewEmbedder linkPreviews = mock(ChatLinkPreviewEmbedder.class);
    ChatTranscriptManualPreviewSupport support =
        new ChatTranscriptManualPreviewSupport(new ChatStyles(null), imageEmbeds, linkPreviews);
    DefaultStyledDocument doc = new DefaultStyledDocument();
    TargetRef ref = new TargetRef("srv", "#chan");
    doc.insertString(0, "hello\n", new SimpleAttributeSet());

    when(imageEmbeds.appendEmbeds(any(), any(), anyString(), anyString(), any()))
        .thenReturn(new ChatImageEmbedder.AppendResult(0, List.of("https://blocked.example/a")));
    when(linkPreviews.appendPreviews(any(), any(), anyString(), anyString(), any()))
        .thenReturn(
            new ChatLinkPreviewEmbedder.AppendResult(0, List.of("https://blocked.example/a")));

    support.appendBlockedPreviewMarkersForAppend(
        ref,
        doc,
        "hello".length(),
        "hello",
        "alice",
        Map.of(),
        lineMeta(),
        null,
        true,
        true,
        (attrs, match) -> new SimpleAttributeSet(attrs));

    String text = doc.getText(0, doc.getLength());
    int marker = text.indexOf("👁");
    assertTrue(marker >= 0);
    assertEquals(-1, text.indexOf("👁", marker + 1));
    assertEquals(
        "https://blocked.example/a",
        doc.getCharacterElement(marker)
            .getAttributes()
            .getAttribute(ChatStyles.ATTR_MANUAL_PREVIEW_URL));
  }

  @Test
  void insertManualPreviewAtFallsBackToLinkPreviewWhenImageInsertDeclines() {
    ChatImageEmbedder imageEmbeds = mock(ChatImageEmbedder.class);
    ChatLinkPreviewEmbedder linkPreviews = mock(ChatLinkPreviewEmbedder.class);
    ChatTranscriptManualPreviewSupport support =
        new ChatTranscriptManualPreviewSupport(new ChatStyles(null), imageEmbeds, linkPreviews);
    DefaultStyledDocument doc = new DefaultStyledDocument();
    TargetRef ref = new TargetRef("srv", "#chan");

    when(imageEmbeds.insertEmbedForUrlAt(any(), any(), anyString(), anyInt())).thenReturn(false);
    when(linkPreviews.insertPreviewForUrlAt(any(), any(), anyString(), anyInt())).thenReturn(true);

    assertTrue(support.insertManualPreviewAt(ref, doc, 0, " https://example.com/x "));
    verify(imageEmbeds).insertEmbedForUrlAt(any(), any(), anyString(), anyInt());
    verify(linkPreviews).insertPreviewForUrlAt(any(), any(), anyString(), anyInt());
  }

  @Test
  void insertManualPreviewAtRejectsBlankUrlAndUiOnlyTargets() {
    ChatTranscriptManualPreviewSupport support =
        new ChatTranscriptManualPreviewSupport(new ChatStyles(null), null, null);
    DefaultStyledDocument doc = new DefaultStyledDocument();

    assertFalse(support.insertManualPreviewAt(new TargetRef("srv", "#chan"), doc, 0, "   "));
    assertFalse(
        support.insertManualPreviewAt(TargetRef.applicationTerminal(), doc, 0, "https://x"));
  }

  private static LineMeta lineMeta() {
    return new LineMeta(
        "srv/#chan", LogKind.CHAT, LogDirection.IN, "alice", 1L, Set.of(), "m-1", "", Map.of());
  }
}
