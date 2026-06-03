package cafe.woden.ircclient.ui.chat.transcript;

import static cafe.woden.ircclient.ui.chat.transcript.support.ChatTranscriptStoreDocumentTestSupport.lineCount;
import static cafe.woden.ircclient.ui.chat.transcript.support.ChatTranscriptStoreDocumentTestSupport.transcriptText;
import static cafe.woden.ircclient.ui.chat.transcript.support.ChatTranscriptStoreTargetRefTestSupport.channelRef;
import static cafe.woden.ircclient.ui.chat.transcript.support.ChatTranscriptStoreTestFactory.newStore;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.app.api.MessageTranslation;
import cafe.woden.ircclient.model.TargetRef;
import java.util.Map;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledDocument;
import org.junit.jupiter.api.Test;

class ChatTranscriptStoreTranslationTest {

  @Test
  void applyMessageTranslationRendersTranslatedTextUnderSourceMessage() throws Exception {
    ChatTranscriptStore store = newStore();
    TargetRef ref = channelRef();

    store.appendChatAt(ref, "alice", "hello everyone", false, 6_000L, "m-1", Map.of());

    assertTrue(
        store.applyMessageTranslation(
            ref, new MessageTranslation("m-1", "hola a todos", "en", "es", "test"), 6_050L));

    String text = transcriptText(store.document(ref));
    assertTrue(text.contains("alice: hello everyone"));
    assertTrue(text.contains("[en -> es via test] hola a todos"));
  }

  @Test
  void applyMessageTranslationUpdatesExistingTranslationRow() throws Exception {
    ChatTranscriptStore store = newStore();
    TargetRef ref = channelRef();

    store.appendChatAt(ref, "alice", "hello", false, 6_000L, "m-1", Map.of());
    assertTrue(
        store.applyMessageTranslation(
            ref, new MessageTranslation("m-1", "hola", "en", "es", ""), 6_050L));
    int translatedLineCount = lineCount(store.document(ref));

    assertTrue(
        store.applyMessageTranslation(
            ref, new MessageTranslation("m-1", "buenas", "en", "es", ""), 6_060L));

    String text = transcriptText(store.document(ref));
    assertEquals(translatedLineCount, lineCount(store.document(ref)));
    assertFalse(text.contains("[en -> es] hola"));
    assertTrue(text.contains("[en -> es] buenas"));
  }

  @Test
  void applyMessageTranslationFindsMessageIdBeyondFirstLineCharacter() throws Exception {
    ChatTranscriptStore store = newStore();
    TargetRef ref = channelRef();

    store.appendChatAt(ref, "alice", "hello everyone", false, 6_000L, "m-1", Map.of());
    StyledDocument doc = store.document(ref);
    doc.setCharacterAttributes(0, 1, new SimpleAttributeSet(), true);

    assertTrue(
        store.applyMessageTranslation(
            ref, new MessageTranslation("m-1", "hola a todos", "en", "es", "test"), 6_050L));

    assertTrue(transcriptText(doc).contains("[en -> es via test] hola a todos"));
  }

  @Test
  void applyMessageTranslationReturnsFalseForUnknownMessageId() {
    ChatTranscriptStore store = newStore();
    TargetRef ref = channelRef();

    assertFalse(
        store.applyMessageTranslation(
            ref, new MessageTranslation("missing", "hola", "en", "es", ""), 6_050L));
  }

  @Test
  void applyMessageRedactionClearsRenderedTranslation() throws Exception {
    ChatTranscriptStore store = newStore();
    TargetRef ref = channelRef();

    store.appendChatAt(ref, "alice", "hello", false, 6_000L, "m-1", Map.of());
    assertTrue(
        store.applyMessageTranslation(
            ref, new MessageTranslation("m-1", "hola", "en", "es", ""), 6_050L));

    assertTrue(store.applyMessageRedaction(ref, "m-1", "alice", 6_100L, "", Map.of()));

    String text = transcriptText(store.document(ref));
    assertTrue(text.contains("[message redacted]"));
    assertFalse(text.contains("[en -> es] hola"));
  }
}
