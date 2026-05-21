package cafe.woden.ircclient.ui.chat.transcript.line;

import static org.junit.jupiter.api.Assertions.assertEquals;

import cafe.woden.ircclient.ui.chat.ChatStyles;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import org.junit.jupiter.api.Test;

class ChatTranscriptDocumentLineSupportTest {

  private final ChatStyles styles = new ChatStyles(null);
  private final ChatTranscriptDocumentLineSupport support =
      new ChatTranscriptDocumentLineSupport(styles);

  @Test
  void ensureAtLineStartAppendsNewlineWithExistingMeta() throws Exception {
    DefaultStyledDocument doc = new DefaultStyledDocument();
    SimpleAttributeSet attrs = new SimpleAttributeSet();
    attrs.addAttribute(ChatStyles.ATTR_META_MSGID, "m-1");
    doc.insertString(0, "hello", attrs);

    support.ensureAtLineStart(doc);

    assertEquals("hello\n", doc.getText(0, doc.getLength()));
    assertEquals(
        "m-1",
        doc.getCharacterElement(doc.getLength() - 1)
            .getAttributes()
            .getAttribute(ChatStyles.ATTR_META_MSGID));
  }

  @Test
  void normalizeInsertAtLineStartSnapsToContainingLineStart() throws Exception {
    DefaultStyledDocument doc = new DefaultStyledDocument();
    doc.insertString(0, "one\ntwo\n", new SimpleAttributeSet());

    assertEquals(0, support.normalizeInsertAtLineStart(doc, 2));
    assertEquals(4, support.normalizeInsertAtLineStart(doc, 5));
    assertEquals(doc.getLength(), support.normalizeInsertAtLineStart(doc, doc.getLength()));
  }

  @Test
  void ensureAtLineStartForInsertCreatesSeparatorWhenInsertFallsMidLine() throws Exception {
    DefaultStyledDocument doc = new DefaultStyledDocument();
    SimpleAttributeSet attrs = new SimpleAttributeSet();
    attrs.addAttribute(ChatStyles.ATTR_META_MSGID, "m-2");
    doc.insertString(0, "hello\n", attrs);

    int next = support.ensureAtLineStartForInsert(doc, 3);

    assertEquals("hel\nlo\n", doc.getText(0, doc.getLength()));
    assertEquals(4, next);
    assertEquals(
        "m-2", doc.getCharacterElement(3).getAttributes().getAttribute(ChatStyles.ATTR_META_MSGID));
  }
}
