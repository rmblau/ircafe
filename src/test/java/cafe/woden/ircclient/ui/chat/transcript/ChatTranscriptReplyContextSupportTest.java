package cafe.woden.ircclient.ui.chat.transcript;

import static org.junit.jupiter.api.Assertions.assertEquals;

import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import javax.swing.text.DefaultStyledDocument;
import org.junit.jupiter.api.Test;

class ChatTranscriptReplyContextSupportTest {

  @Test
  void appendReplyContextLineWritesMsgRefAndPreview() throws Exception {
    ChatTranscriptReplyContextSupport.Context context =
        new ChatTranscriptReplyContextSupport.Context(
            new ChatStyles(null), null, (ref, fromNick) -> "Alice");
    DefaultStyledDocument doc = new DefaultStyledDocument();
    TargetRef ref = new TargetRef("srv", "#chan");

    ChatTranscriptReplyContextSupport.appendReplyContextLine(
        context, doc, ref, "alice", " m-1 ", 1_234L, messageId -> "alice: hello");

    assertEquals("-> Alice replied to m-1 (alice: hello)\n", doc.getText(0, doc.getLength()));
    int msgRefOffset = doc.getText(0, doc.getLength()).indexOf("m-1");
    assertEquals(
        "m-1",
        doc.getCharacterElement(msgRefOffset)
            .getAttributes()
            .getAttribute(ChatStyles.ATTR_MSG_REF));
  }

  @Test
  void appendReplyContextLineSkipsBlankReplyIds() throws Exception {
    ChatTranscriptReplyContextSupport.Context context =
        new ChatTranscriptReplyContextSupport.Context(
            new ChatStyles(null), null, (ref, fromNick) -> "");
    DefaultStyledDocument doc = new DefaultStyledDocument();

    ChatTranscriptReplyContextSupport.appendReplyContextLine(
        context,
        doc,
        new TargetRef("srv", "#chan"),
        "alice",
        "   ",
        1_234L,
        messageId -> "ignored");

    assertEquals("", doc.getText(0, doc.getLength()));
  }
}
