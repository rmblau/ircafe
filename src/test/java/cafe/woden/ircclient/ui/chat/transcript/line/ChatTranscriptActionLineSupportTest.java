package cafe.woden.ircclient.ui.chat.transcript.line;

import static org.junit.jupiter.api.Assertions.assertEquals;

import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTimestampFormatter;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import org.junit.jupiter.api.Test;

class ChatTranscriptActionLineSupportTest {

  private final ChatTimestampFormatter timestamps = new ChatTimestampFormatter(null, null);

  @Test
  void writeLineAtAddsTimestampPrefixAndActionPrefix() throws Exception {
    DefaultStyledDocument doc = new DefaultStyledDocument();
    SimpleAttributeSet tsStyle = new SimpleAttributeSet();
    SimpleAttributeSet fromStyle = new SimpleAttributeSet();
    SimpleAttributeSet messageStyle = new SimpleAttributeSet();

    ChatTranscriptActionLineSupport.WriteResult result =
        ChatTranscriptActionLineSupport.writeLineAt(
            doc,
            new TargetRef("srv", "#chan"),
            0,
            "waves",
            "Alice",
            tsStyle,
            fromStyle,
            messageStyle,
            1_234L,
            timestamps,
            null,
            true,
            false);

    String expected = timestamps.prefixAt(1_234L) + "* Alice waves\n";
    assertEquals(expected, doc.getText(0, doc.getLength()));
    assertEquals(expected.length() - 1, result.lineEndOffset());
    assertEquals(expected.length(), result.nextOffset());
  }

  @Test
  void writeLineAtSkipsTimestampWhenDisabledForActionLines() throws Exception {
    DefaultStyledDocument doc = new DefaultStyledDocument();
    doc.insertString(0, "tail\n", new SimpleAttributeSet());

    ChatTranscriptActionLineSupport.WriteResult result =
        ChatTranscriptActionLineSupport.writeLineAt(
            doc,
            new TargetRef("srv", "#chan"),
            0,
            "waves",
            "Alice",
            new SimpleAttributeSet(),
            new SimpleAttributeSet(),
            new SimpleAttributeSet(),
            1_234L,
            timestamps,
            null,
            false,
            false);

    assertEquals("* Alice waves\ntail\n", doc.getText(0, doc.getLength()));
    assertEquals(13, result.lineEndOffset());
    assertEquals(14, result.nextOffset());
  }

  @Test
  void writeLineAtOmitsSenderWhenBlank() throws Exception {
    DefaultStyledDocument doc = new DefaultStyledDocument();

    ChatTranscriptActionLineSupport.writeLineAt(
        doc,
        new TargetRef("srv", "#chan"),
        0,
        "waves",
        "",
        new SimpleAttributeSet(),
        new SimpleAttributeSet(),
        new SimpleAttributeSet(),
        1_234L,
        timestamps,
        null,
        false,
        false);

    assertEquals("* waves\n", doc.getText(0, doc.getLength()));
  }
}
