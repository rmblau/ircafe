package cafe.woden.ircclient.ui.chat.transcript;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.ui.chat.ChatStyles;
import javax.swing.JLabel;
import javax.swing.text.AttributeSet;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import org.junit.jupiter.api.Test;

class ChatTranscriptDocumentSupportTest {

  @Test
  void findLineStartByMessageIdMatchesNormalizedMessageIdAtLineStart() throws Exception {
    DefaultStyledDocument doc = new DefaultStyledDocument();
    SimpleAttributeSet firstLine = new SimpleAttributeSet();
    firstLine.addAttribute(ChatStyles.ATTR_META_MSGID, "m-1");

    doc.insertString(0, "alice: hello\n", firstLine);
    doc.insertString(doc.getLength(), "bob: hi\n", new SimpleAttributeSet());

    assertEquals(0, ChatTranscriptDocumentSupport.findLineStartByMessageId(doc, " m-1 "));
    assertEquals(-1, ChatTranscriptDocumentSupport.findLineStartByMessageId(doc, "m-2"));
  }

  @Test
  void findMessageLineReturnsNormalizedIdAndAttrs() throws Exception {
    DefaultStyledDocument doc = new DefaultStyledDocument();
    SimpleAttributeSet firstLine = new SimpleAttributeSet();
    firstLine.addAttribute(ChatStyles.ATTR_META_MSGID, "m-1");
    firstLine.addAttribute(ChatStyles.ATTR_META_FROM, "alice");

    doc.insertString(0, "alice: hello\n", firstLine);

    ChatTranscriptDocumentSupport.MessageLine line =
        ChatTranscriptDocumentSupport.findMessageLine(doc, " m-1 ");

    assertNotNull(line);
    assertEquals("m-1", line.targetMessageId());
    assertEquals(0, line.lineStart());
    AttributeSet attrs = line.attrs();
    assertEquals("alice", attrs.getAttribute(ChatStyles.ATTR_META_FROM));
  }

  @Test
  void findLineStartByPendingIdScansAcrossWholeLine() throws Exception {
    DefaultStyledDocument doc = new DefaultStyledDocument();
    doc.insertString(0, "me: ", new SimpleAttributeSet());
    SimpleAttributeSet pending = new SimpleAttributeSet();
    pending.addAttribute(ChatStyles.ATTR_META_PENDING_ID, "pending-1");
    doc.insertString(doc.getLength(), "hello", pending);
    doc.insertString(doc.getLength(), "\n", new SimpleAttributeSet());

    assertEquals(0, ChatTranscriptDocumentSupport.findLineStartByPendingId(doc, " pending-1 "));
    assertEquals(-1, ChatTranscriptDocumentSupport.findLineStartByPendingId(doc, "pending-2"));
  }

  @Test
  void lineEndOffsetForLineStartClampsToDocumentBounds() throws Exception {
    DefaultStyledDocument doc = new DefaultStyledDocument();
    doc.insertString(0, "one\nsecond\n", new SimpleAttributeSet());

    assertEquals(4, ChatTranscriptDocumentSupport.lineEndOffsetForLineStart(doc, 0));
    assertEquals(doc.getLength(), ChatTranscriptDocumentSupport.lineEndOffsetForLineStart(doc, 4));
    assertEquals(999, ChatTranscriptDocumentSupport.lineEndOffsetForLineStart(null, 999));
  }

  @Test
  void findInlineComponentOffsetMatchesExpectedComponentWithinRange() throws Exception {
    DefaultStyledDocument doc = new DefaultStyledDocument();
    JLabel marker = new JLabel("marker");
    SimpleAttributeSet componentAttrs = new SimpleAttributeSet();
    StyleConstants.setComponent(componentAttrs, marker);

    doc.insertString(0, "a", new SimpleAttributeSet());
    doc.insertString(doc.getLength(), " ", componentAttrs);
    doc.insertString(doc.getLength(), "b", new SimpleAttributeSet());

    int offset = ChatTranscriptDocumentSupport.findInlineComponentOffset(doc, 2, 0, marker);

    assertEquals(1, offset);
    assertTrue(
        ChatTranscriptDocumentSupport.findInlineComponentOffset(doc, 0, 2, new JLabel("x")) < 0);
  }

  @Test
  void markLineRangeRedactedAddsRedactedMetadataAcrossLine() throws Exception {
    DefaultStyledDocument doc = new DefaultStyledDocument();
    SimpleAttributeSet message = new SimpleAttributeSet();
    message.addAttribute(ChatStyles.ATTR_META_MSGID, "m-1");
    doc.insertString(0, "alice: hello\n", message);
    doc.insertString(doc.getLength(), "bob: hi\n", new SimpleAttributeSet());

    ChatTranscriptDocumentSupport.markLineRangeRedacted(doc, 0);

    assertEquals(
        Boolean.TRUE,
        doc.getCharacterElement(0).getAttributes().getAttribute(ChatStyles.ATTR_META_REDACTED));
    assertFalse(
        Boolean.TRUE.equals(
            doc.getCharacterElement("alice: hello\n".length())
                .getAttributes()
                .getAttribute(ChatStyles.ATTR_META_REDACTED)));
  }
}
