package cafe.woden.ircclient.ui.chat.transcript;

import static cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStoreManualPreviewTestSupport.newManualPreviewFallbackFixture;
import static cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStoreManualPreviewTestSupport.newStoreWithBlockedImagePreview;
import static cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStoreManualPreviewTestSupport.verifyManualPreviewFallbackAttempted;
import static cafe.woden.ircclient.ui.chat.transcript.support.ChatTranscriptStoreTargetRefTestSupport.channelRef;
import static cafe.woden.ircclient.ui.chat.transcript.support.ChatTranscriptStoreDocumentTestSupport.transcriptText;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import javax.swing.text.StyledDocument;
import org.junit.jupiter.api.Test;

class ChatTranscriptStoreManualPreviewTest {

  @Test
  void appendChatAtAddsManualPreviewMarkerForPolicyBlockedUrls() throws Exception {
    ChatTranscriptStore store = newStoreWithBlockedImagePreview("https://blocked.example/a.png");
    TargetRef ref = channelRef();

    store.appendChatAt(ref, "alice", "https://blocked.example/a.png", false, 9_000L);

    StyledDocument doc = store.document(ref);
    String text = transcriptText(doc);
    int marker = text.indexOf("👁");
    assertTrue(marker >= 0);
    Object markerUrl =
        doc.getCharacterElement(marker)
            .getAttributes()
            .getAttribute(ChatStyles.ATTR_MANUAL_PREVIEW_URL);
    assertEquals("https://blocked.example/a.png", markerUrl);
  }

  @Test
  void insertManualPreviewAtFallsBackToLinkPreviewWhenImageInsertDeclines() {
    ChatTranscriptStoreManualPreviewTestSupport.ManualPreviewFallbackFixture fixture =
        newManualPreviewFallbackFixture();
    ChatTranscriptStore store = fixture.store();
    TargetRef ref = channelRef();
    store.appendChat(ref, "alice", "line");

    assertTrue(store.insertManualPreviewAt(ref, 0, "https://example.com/x"));
    verifyManualPreviewFallbackAttempted(fixture);
  }
}
