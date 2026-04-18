package cafe.woden.ircclient.ui.chat.transcript;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.model.LogDirection;
import cafe.woden.ircclient.model.LogKind;
import cafe.woden.ircclient.model.TargetRef;
import java.util.HashMap;
import java.util.Map;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import org.junit.jupiter.api.Test;

class ChatTranscriptMessageQuerySupportTest {

  private final ChatTranscriptMessageStateSupport.Context messageStateContext =
      new ChatTranscriptMessageStateSupport.Context(120, "[redacted]", () -> 1L);
  private final ChatTranscriptMessageCatalogSupport messageCatalogSupport =
      new ChatTranscriptMessageCatalogSupport(messageStateContext);
  private final ChatTranscriptMessageQuerySupport support = new ChatTranscriptMessageQuerySupport();

  @Test
  void messageOffsetAndOwnMessageUseDocumentMetadata() throws Exception {
    TargetRef ref = new TargetRef("srv", "#chan");
    DefaultStyledDocument doc = new DefaultStyledDocument();
    SimpleAttributeSet attrs =
        ChatTranscriptLineMetaSupport.bind(
            new SimpleAttributeSet(),
            ChatTranscriptLineMetaSupport.create(
                ref, LogKind.CHAT, LogDirection.OUT, "alice", 123L, null, "m-1", Map.of()));
    attrs.addAttribute(cafe.woden.ircclient.ui.chat.ChatStyles.ATTR_OUTGOING, Boolean.TRUE);
    doc.insertString(0, "alice: hello\n", attrs);

    Map<TargetRef, javax.swing.text.StyledDocument> docs = new HashMap<>();
    docs.put(ref, doc);
    Map<TargetRef, ChatTranscriptState> states = new HashMap<>();
    states.put(ref, newState());
    ChatTranscriptMessageQuerySupport.Context context =
        new ChatTranscriptMessageQuerySupport.Context(docs, states, messageCatalogSupport);

    assertEquals(0, support.messageOffsetById(context, ref, "m-1"));
    assertTrue(support.isOwnMessage(context, ref, "m-1"));
    assertFalse(support.isOwnMessage(context, ref, "missing"));
  }

  @Test
  void messagePreviewUsesCatalogState() {
    TargetRef ref = new TargetRef("srv", "#chan");
    ChatTranscriptState state = newState();
    messageCatalogSupport.recordInsertedMessage(
        state.messageCatalog,
        ChatTranscriptLineMetaSupport.create(
            ref, LogKind.CHAT, LogDirection.IN, "alice", 321L, null, "m-2", Map.of()),
        "alice",
        "hello there");

    ChatTranscriptMessageQuerySupport.Context context =
        new ChatTranscriptMessageQuerySupport.Context(Map.of(), Map.of(ref, state), messageCatalogSupport);

    assertEquals("alice: hello there", support.messagePreviewById(context, ref, "m-2"));
    assertEquals("", support.messagePreviewById(context, ref, "missing"));
  }

  private ChatTranscriptState newState() {
    return new ChatTranscriptState(
        messageCatalogSupport.createState(32, 32),
        new ChatTranscriptFilteredLinesSupport.State(),
        new ChatTranscriptPresenceFoldSupport.State());
  }
}
