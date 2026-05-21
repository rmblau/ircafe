package cafe.woden.ircclient.ui.chat.transcript.line;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTimestampFormatter;
import javax.swing.JLabel;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import org.junit.jupiter.api.Test;

class ChatTranscriptTextLineSupportTest {

  private final ChatTimestampFormatter timestamps = new ChatTimestampFormatter(null, null);

  @Test
  void writeLineAtAddsTimestampFromAndNewlineForChatMessages() throws Exception {
    DefaultStyledDocument doc = new DefaultStyledDocument();
    SimpleAttributeSet tsStyle = new SimpleAttributeSet();
    SimpleAttributeSet fromStyle = new SimpleAttributeSet();
    SimpleAttributeSet msgStyle = new SimpleAttributeSet();
    msgStyle.addAttribute(ChatStyles.ATTR_STYLE, ChatStyles.STYLE_MESSAGE);

    ChatTranscriptTextLineSupport.WriteResult result =
        ChatTranscriptTextLineSupport.writeLineAt(
            doc,
            new TargetRef("srv", "#chan"),
            0,
            "hello",
            "Alice",
            tsStyle,
            fromStyle,
            msgStyle,
            1_234L,
            timestamps,
            null,
            true,
            false,
            false,
            null,
            null);

    String expected = timestamps.prefixAt(1_234L) + "Alice: hello\n";
    assertEquals(expected, doc.getText(0, doc.getLength()));
    assertEquals(expected.length() - 1, result.lineEndOffset());
    assertEquals(expected.length(), result.nextOffset());
  }

  @Test
  void writeLineAtSkipsTimestampForUnsupportedStyleAndPreservesInsertOffset() throws Exception {
    DefaultStyledDocument doc = new DefaultStyledDocument();
    doc.insertString(0, "tail\n", new SimpleAttributeSet());

    SimpleAttributeSet tsStyle = new SimpleAttributeSet();
    SimpleAttributeSet fromStyle = new SimpleAttributeSet();
    SimpleAttributeSet msgStyle = new SimpleAttributeSet();
    msgStyle.addAttribute(ChatStyles.ATTR_STYLE, ChatStyles.STYLE_LINK);

    ChatTranscriptTextLineSupport.WriteResult result =
        ChatTranscriptTextLineSupport.writeLineAt(
            doc,
            new TargetRef("srv", "#chan"),
            0,
            "head",
            "",
            tsStyle,
            fromStyle,
            msgStyle,
            1_234L,
            timestamps,
            null,
            true,
            true,
            false,
            null,
            null);

    assertEquals("head\ntail\n", doc.getText(0, doc.getLength()));
    assertEquals(4, result.lineEndOffset());
    assertEquals(5, result.nextOffset());
  }

  @Test
  void writeLineAtPlacesTailComponentBeforeTheNewline() throws Exception {
    DefaultStyledDocument doc = new DefaultStyledDocument();
    SimpleAttributeSet tsStyle = new SimpleAttributeSet();
    SimpleAttributeSet fromStyle = new SimpleAttributeSet();
    SimpleAttributeSet msgStyle = new SimpleAttributeSet();
    msgStyle.addAttribute(ChatStyles.ATTR_STYLE, ChatStyles.STYLE_MESSAGE);
    JLabel tail = new JLabel("tail");

    ChatTranscriptTextLineSupport.WriteResult result =
        ChatTranscriptTextLineSupport.writeLineAt(
            doc,
            new TargetRef("srv", "#chan"),
            0,
            "hello",
            "Alice",
            tsStyle,
            fromStyle,
            msgStyle,
            1_234L,
            timestamps,
            null,
            true,
            false,
            false,
            tail,
            msgStyle);

    assertEquals(timestamps.prefixAt(1_234L) + "Alice: hello \n", doc.getText(0, doc.getLength()));
    assertSame(
        tail,
        StyleConstants.getComponent(
            doc.getCharacterElement(result.lineEndOffset() - 1).getAttributes()));
  }
}
