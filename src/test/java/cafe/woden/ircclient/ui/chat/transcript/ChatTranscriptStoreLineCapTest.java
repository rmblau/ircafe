package cafe.woden.ircclient.ui.chat.transcript;

import static cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStoreDocumentTestSupport.lineCount;
import static cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStoreDocumentTestSupport.transcriptText;
import static cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStoreTargetRefTestSupport.channelRef;
import static cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStoreTestFactory.newStoreWithTranscriptCap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.model.TargetRef;
import javax.swing.text.StyledDocument;
import org.junit.jupiter.api.Test;

class ChatTranscriptStoreLineCapTest {

  @Test
  void appendChatAtTrimsOldestLinesWhenTranscriptCapIsExceeded() throws Exception {
    ChatTranscriptStore store = newStoreWithTranscriptCap(2);
    TargetRef ref = channelRef();

    store.appendChatAt(ref, "alice", "line-1", false, 7_000L);
    store.appendChatAt(ref, "alice", "line-2", false, 7_010L);
    store.appendChatAt(ref, "alice", "line-3", false, 7_020L);

    StyledDocument doc = store.document(ref);
    String text = transcriptText(doc);
    assertFalse(text.contains("line-1"));
    assertTrue(text.contains("line-2"));
    assertTrue(text.contains("line-3"));
    assertEquals(2, lineCount(doc));
  }

  @Test
  void transcriptCapZeroDisablesHeadTrimming() throws Exception {
    ChatTranscriptStore store = newStoreWithTranscriptCap(0);
    TargetRef ref = channelRef();

    store.appendChatAt(ref, "alice", "line-1", false, 8_000L);
    store.appendChatAt(ref, "alice", "line-2", false, 8_010L);
    store.appendChatAt(ref, "alice", "line-3", false, 8_020L);

    StyledDocument doc = store.document(ref);
    String text = transcriptText(doc);
    assertTrue(text.contains("line-1"));
    assertTrue(text.contains("line-2"));
    assertTrue(text.contains("line-3"));
    assertEquals(3, lineCount(doc));
  }
}
