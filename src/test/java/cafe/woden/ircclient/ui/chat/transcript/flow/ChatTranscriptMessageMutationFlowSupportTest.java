package cafe.woden.ircclient.ui.chat.transcript;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.transcript.filter.ChatTranscriptFilteredLinesSupport;
import cafe.woden.ircclient.ui.chat.transcript.flow.ChatTranscriptMessageMutationFlowSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptPresenceFoldSupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageCatalogSupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageMutationSupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageStateSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptState;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyledDocument;
import org.junit.jupiter.api.Test;

class ChatTranscriptMessageMutationFlowSupportTest {

  @Test
  void applyMessageEditEnsuresTargetAndDelegatesCatalogState() {
    ChatTranscriptMessageMutationSupport mutationSupport =
        mock(ChatTranscriptMessageMutationSupport.class);
    ChatTranscriptMessageMutationFlowSupport support =
        new ChatTranscriptMessageMutationFlowSupport();
    TargetRef ref = new TargetRef("srv", "#chan");
    Map<TargetRef, StyledDocument> docs = new HashMap<>();
    StyledDocument doc = new DefaultStyledDocument();
    docs.put(ref, doc);
    ChatTranscriptState state = newState();
    Map<TargetRef, ChatTranscriptState> states = new HashMap<>();
    states.put(ref, state);
    AtomicBoolean ensured = new AtomicBoolean(false);
    ChatTranscriptMessageMutationFlowSupport.Context context =
        new ChatTranscriptMessageMutationFlowSupport.Context(
            docs,
            states,
            target -> {
              if (ref.equals(target)) {
                ensured.set(true);
              }
            },
            mutationSupport);
    when(mutationSupport.applyMessageEdit(
            eq(ref),
            eq(doc),
            eq(state.messageCatalog()),
            eq("m-1"),
            eq("edited"),
            eq(10L),
            eq("m-2"),
            eq(Map.of("msgid", "m-2"))))
        .thenReturn(true);

    boolean applied =
        support.applyMessageEdit(
            context, ref, "m-1", "edited", "alice", 10L, "m-2", Map.of("msgid", "m-2"));

    assertTrue(applied);
    assertTrue(ensured.get());
    verify(mutationSupport)
        .applyMessageEdit(
            ref, doc, state.messageCatalog(), "m-1", "edited", 10L, "m-2", Map.of("msgid", "m-2"));
  }

  @Test
  void applyMessageRedactionReturnsFalseWhenRefMissing() {
    ChatTranscriptMessageMutationSupport mutationSupport =
        mock(ChatTranscriptMessageMutationSupport.class);
    ChatTranscriptMessageMutationFlowSupport support =
        new ChatTranscriptMessageMutationFlowSupport();
    ChatTranscriptMessageMutationFlowSupport.Context context =
        new ChatTranscriptMessageMutationFlowSupport.Context(
            Map.of(), Map.of(), target -> {}, mutationSupport);

    assertFalse(
        support.applyMessageRedaction(
            context, null, "m-1", "alice", 12L, "m-2", Map.of("msgid", "m-2")));
  }

  private ChatTranscriptState newState() {
    ChatTranscriptMessageCatalogSupport messageCatalogSupport =
        new ChatTranscriptMessageCatalogSupport(
            new ChatTranscriptMessageStateSupport.Context(120, "[redacted]", () -> 1L));
    return new ChatTranscriptState(
        messageCatalogSupport.createState(32, 32),
        new ChatTranscriptFilteredLinesSupport.State(),
        new ChatTranscriptPresenceFoldSupport.State());
  }
}
