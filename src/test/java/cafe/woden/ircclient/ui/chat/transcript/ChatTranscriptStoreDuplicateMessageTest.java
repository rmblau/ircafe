package cafe.woden.ircclient.ui.chat.transcript;

import static cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStoreTargetRefTestSupport.channelRef;
import static cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStoreTargetRefTestSupport.statusRef;
import static cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStoreTestFactory.newStore;
import static cafe.woden.ircclient.ui.chat.transcript.support.ChatTranscriptStoreDocumentTestSupport.lineCount;
import static cafe.woden.ircclient.ui.chat.transcript.support.ChatTranscriptStoreDocumentTestSupport.transcriptText;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.model.TargetRef;
import java.util.Map;
import javax.swing.text.StyledDocument;
import org.junit.jupiter.api.Test;

class ChatTranscriptStoreDuplicateMessageTest {

  @Test
  void appendChatAtWithDuplicateMessageIdIsIgnored() throws Exception {
    ChatTranscriptStore store = newStore();
    TargetRef ref = channelRef();

    store.appendChatAt(ref, "alice", "first", false, 1_000L, "m-1", Map.of("msgid", "m-1"));
    StyledDocument doc = store.document(ref);
    int lenAfterFirst = doc.getLength();

    store.appendChatAt(ref, "alice", "second", false, 1_050L, "m-1", Map.of("msgid", "m-1"));

    assertEquals(lenAfterFirst, doc.getLength());
    assertTrue(transcriptText(doc).contains("first"));
    assertFalse(transcriptText(doc).contains("second"));
    assertTrue(store.messageOffsetById(ref, "m-1") >= 0);
  }

  @Test
  void appendActionAtWithDuplicateMessageIdIsIgnored() throws Exception {
    ChatTranscriptStore store = newStore();
    TargetRef ref = channelRef();

    store.appendActionAt(ref, "alice", "waves", false, 2_000L, "act-1", Map.of("msgid", "act-1"));
    StyledDocument doc = store.document(ref);
    int lenAfterFirst = doc.getLength();

    store.appendActionAt(ref, "alice", "jumps", false, 2_050L, "act-1", Map.of("msgid", "act-1"));

    assertEquals(lenAfterFirst, doc.getLength());
    assertTrue(transcriptText(doc).contains("waves"));
    assertFalse(transcriptText(doc).contains("jumps"));
  }

  @Test
  void appendNoticeAtWithDuplicateMessageIdIsIgnored() throws Exception {
    ChatTranscriptStore store = newStore();
    TargetRef ref = statusRef();

    store.appendNoticeAt(
        ref, "(notice) server", "maintenance", 3_000L, "n-1", Map.of("msgid", "n-1"));
    StyledDocument doc = store.document(ref);
    int lenAfterFirst = doc.getLength();

    store.appendNoticeAt(ref, "(notice) server", "new text", 3_100L, "n-1", Map.of("msgid", "n-1"));

    assertEquals(lenAfterFirst, doc.getLength());
    assertTrue(transcriptText(doc).contains("maintenance"));
    assertFalse(transcriptText(doc).contains("new text"));
  }

  @Test
  void appendStatusAtWithDuplicateMessageIdIsIgnored() throws Exception {
    ChatTranscriptStore store = newStore();
    TargetRef ref = statusRef();

    store.appendStatusAt(
        ref, "(server)", "421 NO_SUCH_COMMAND", 4_000L, "s-1", Map.of("msgid", "s-1"));
    StyledDocument doc = store.document(ref);
    int lenAfterFirst = doc.getLength();

    store.appendStatusAt(
        ref, "(server)", "different status", 4_100L, "s-1", Map.of("msgid", "s-1"));

    assertEquals(lenAfterFirst, doc.getLength());
    assertTrue(transcriptText(doc).contains("421 NO_SUCH_COMMAND"));
    assertFalse(transcriptText(doc).contains("different status"));
  }

  @Test
  void blankMessageIdDoesNotSuppressRepeatedMessages() {
    ChatTranscriptStore store = newStore();
    TargetRef ref = channelRef();

    store.appendChatAt(ref, "alice", "same", false, 5_000L, "", Map.of());
    store.appendChatAt(ref, "alice", "same", false, 5_010L, "", Map.of());

    assertEquals(2, lineCount(store.document(ref)));
  }

  @Test
  void appendChatFromHistoryWithDuplicateMessageIdIsIgnored() throws Exception {
    ChatTranscriptStore store = newStore();
    TargetRef ref = channelRef();

    store.appendChatFromHistory(
        ref, "alice", "first", false, 5_000L, "m-h1", Map.of("msgid", "m-h1"));
    StyledDocument doc = store.document(ref);
    int lenAfterFirst = doc.getLength();

    store.appendChatFromHistory(
        ref, "alice", "second", false, 5_010L, "m-h1", Map.of("msgid", "m-h1"));

    assertEquals(lenAfterFirst, doc.getLength());
    assertTrue(transcriptText(doc).contains("first"));
    assertFalse(transcriptText(doc).contains("second"));
  }
}
