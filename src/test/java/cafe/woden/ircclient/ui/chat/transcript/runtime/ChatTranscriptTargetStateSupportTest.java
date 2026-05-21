package cafe.woden.ircclient.ui.chat.transcript.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageCatalogSupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageStateSupport;
import java.util.HashMap;
import java.util.Map;
import javax.swing.text.StyledDocument;
import org.junit.jupiter.api.Test;

class ChatTranscriptTargetStateSupportTest {

  @Test
  void ensureTargetCreatesDocumentAndTracksEarliestEpoch() {
    Map<TargetRef, StyledDocument> docs = new HashMap<>();
    Map<TargetRef, ChatTranscriptState> states = new HashMap<>();
    ChatTranscriptTargetStateSupport support = newSupport(docs, states);
    TargetRef ref = new TargetRef("srv", "#chan");

    support.ensureTargetExists(ref);
    support.noteEpochMs(ref, 2_000L);
    support.noteEpochMs(ref, 1_000L);

    assertNotNull(docs.get(ref));
    assertNotNull(states.get(ref));
    assertEquals(1_000L, support.earliestTimestampEpochMs(ref).orElseThrow());
  }

  @Test
  void closeTargetRemovesDocumentAndState() {
    Map<TargetRef, StyledDocument> docs = new HashMap<>();
    Map<TargetRef, ChatTranscriptState> states = new HashMap<>();
    ChatTranscriptTargetStateSupport support = newSupport(docs, states);
    TargetRef ref = new TargetRef("srv", "#chan");

    support.ensureTargetExists(ref);
    support.closeTarget(ref);

    assertFalse(docs.containsKey(ref));
    assertFalse(states.containsKey(ref));
  }

  @Test
  void clearTargetEmptiesDocumentAndResetsState() throws Exception {
    Map<TargetRef, StyledDocument> docs = new HashMap<>();
    Map<TargetRef, ChatTranscriptState> states = new HashMap<>();
    ChatTranscriptTargetStateSupport support = newSupport(docs, states);
    TargetRef ref = new TargetRef("srv", "#chan");

    support.ensureTargetExists(ref);
    StyledDocument doc = docs.get(ref);
    ChatTranscriptState originalState = states.get(ref);
    doc.insertString(0, "hello", null);
    originalState.noteEpochMs(1_000L);

    support.clearTarget(ref);

    assertEquals(0, docs.get(ref).getLength());
    assertNotNull(states.get(ref));
    assertNotSame(originalState, states.get(ref));
    assertFalse(support.earliestTimestampEpochMs(ref).isPresent());
  }

  private static ChatTranscriptTargetStateSupport newSupport(
      Map<TargetRef, StyledDocument> docs, Map<TargetRef, ChatTranscriptState> states) {
    ChatTranscriptMessageCatalogSupport messageCatalogSupport =
        new ChatTranscriptMessageCatalogSupport(
            new ChatTranscriptMessageStateSupport.Context(120, "[redacted]", () -> 1L));
    return new ChatTranscriptTargetStateSupport(
        docs, states, () -> messageCatalogSupport.createState(32, 32), new Object());
  }
}
