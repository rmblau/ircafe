package cafe.woden.ircclient.ui.chat.transcript;

import static cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStoreTargetRefTestSupport.channelRef;
import static cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStoreTestFactory.newStore;
import static cafe.woden.ircclient.ui.chat.transcript.support.ChatTranscriptStoreDocumentTestSupport.transcriptText;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.util.EmojiFontSupport;
import javax.swing.text.StyledDocument;
import org.junit.jupiter.api.Test;

class ChatTranscriptStoreEmojiTest {

  @Test
  void appendChatAtMarksEmojiGlyphRunsInTranscript() throws Exception {
    ChatTranscriptStore store = newStore();
    TargetRef ref = channelRef();

    store.appendChatAt(ref, "alice", "hello 😀 world", false, 6_500L);

    StyledDocument doc = store.document(ref);
    String text = transcriptText(doc);
    int emojiIndex = text.indexOf("😀");
    assertTrue(emojiIndex >= 0);
    assertTrue(EmojiFontSupport.isEmojiRun(doc.getCharacterElement(emojiIndex).getAttributes()));
    assertFalse(
        EmojiFontSupport.isEmojiRun(
            doc.getCharacterElement(text.indexOf("hello")).getAttributes()));
  }
}
