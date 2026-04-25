package cafe.woden.ircclient.ui.chat.transcript.runtime;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.transcript.filter.ChatTranscriptFilteredLinesSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptAuxiliaryRowsSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptPresenceFoldSupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageCatalogSupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageStateSupport;
import java.util.HashMap;
import java.util.Map;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyledDocument;
import org.junit.jupiter.api.Test;

class ChatTranscriptLifecycleSupportTest {

  @Test
  void maybeRenderPendingReadMarkerDelegatesWithTargetDocumentAndAuxiliaryState() {
    TargetRef ref = new TargetRef("srv", "#chan");
    StyledDocument doc = new DefaultStyledDocument();
    ChatTranscriptState state = newTranscriptState();
    Map<TargetRef, StyledDocument> docs = new HashMap<>();
    docs.put(ref, doc);
    Map<TargetRef, ChatTranscriptState> states = new HashMap<>();
    states.put(ref, state);
    ChatTranscriptAuxiliaryRowsSupport auxiliaryRowsSupport =
        mock(ChatTranscriptAuxiliaryRowsSupport.class);
    ChatTranscriptLifecycleSupport.Context context =
        new ChatTranscriptLifecycleSupport.Context(
            docs,
            states,
            ChatTranscriptLifecycleSupportTest::newTranscriptState,
            auxiliaryRowsSupport,
            target -> {},
            target -> {});

    new ChatTranscriptLifecycleSupport().maybeRenderPendingReadMarker(context, ref, 2_000L);

    verify(auxiliaryRowsSupport)
        .maybeRenderPendingReadMarker(ref, doc, state.auxiliaryRows(), 2_000L);
  }

  private static ChatTranscriptState newTranscriptState() {
    ChatTranscriptMessageCatalogSupport messageCatalogSupport =
        new ChatTranscriptMessageCatalogSupport(
            new ChatTranscriptMessageStateSupport.Context(120, "[message redacted]", () -> 1L));
    return new ChatTranscriptState(
        messageCatalogSupport.createState(8, 8),
        new ChatTranscriptFilteredLinesSupport.State(),
        new ChatTranscriptPresenceFoldSupport.State());
  }
}
