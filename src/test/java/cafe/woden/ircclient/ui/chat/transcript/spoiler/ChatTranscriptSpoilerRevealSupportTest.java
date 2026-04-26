package cafe.woden.ircclient.ui.chat.transcript.spoiler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.model.LogDirection;
import cafe.woden.ircclient.model.LogKind;
import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.fold.SpoilerMessageComponent;
import cafe.woden.ircclient.ui.chat.transcript.line.LineMeta;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptLineMetaSupport;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Position;
import javax.swing.text.SimpleAttributeSet;
import org.junit.jupiter.api.Test;

class ChatTranscriptSpoilerRevealSupportTest {

  @Test
  void revealInPlaceReplacesSpoilerPlaceholderWithRenderedLine() throws Exception {
    ChatStyles styles = new ChatStyles(null);
    TargetRef ref = new TargetRef("srv", "#chan");
    LineMeta meta =
        ChatTranscriptLineMetaSupport.create(
            ref, LogKind.SPOILER, LogDirection.IN, "alice", 1234L, null);
    DefaultStyledDocument doc = new DefaultStyledDocument();
    SpoilerMessageComponent component = new SpoilerMessageComponent("[12:00] ", "Alice: ");

    ChatTranscriptSpoilerLineSupport.writeLineAt(
        doc,
        0,
        component,
        ChatTranscriptLineMetaSupport.bind(styles.message(), meta),
        ChatTranscriptLineMetaSupport.bind(styles.timestamp(), meta),
        pos -> () -> false);

    Position anchor = doc.createPosition(0);
    boolean revealed =
        ChatTranscriptSpoilerRevealSupport.revealInPlace(
            new ChatTranscriptSpoilerRevealSupport.Context(
                styles, null, null, (target, fromNick) -> "Alice"),
            doc,
            ref,
            anchor,
            component,
            "[12:00] ",
            "alice",
            "hello");

    assertTrue(revealed);
    assertEquals("[12:00] Alice: hello\n", doc.getText(0, doc.getLength()));
  }

  @Test
  void revealInPlaceReturnsFalseWhenExpectedSpoilerDoesNotMatch() throws Exception {
    ChatStyles styles = new ChatStyles(null);
    TargetRef ref = new TargetRef("srv", "#chan");
    DefaultStyledDocument doc = new DefaultStyledDocument();
    SpoilerMessageComponent inserted = new SpoilerMessageComponent("", "Alice: ");

    ChatTranscriptSpoilerLineSupport.writeLineAt(
        doc, 0, inserted, new SimpleAttributeSet(), new SimpleAttributeSet(), pos -> () -> false);

    Position anchor = doc.createPosition(0);
    boolean revealed =
        ChatTranscriptSpoilerRevealSupport.revealInPlace(
            new ChatTranscriptSpoilerRevealSupport.Context(
                styles, null, null, (target, fromNick) -> fromNick),
            doc,
            ref,
            anchor,
            new SpoilerMessageComponent("", "Alice: "),
            "",
            "alice",
            "hello");

    assertFalse(revealed);
    assertEquals(" \n", doc.getText(0, doc.getLength()));
  }
}
