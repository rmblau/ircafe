package cafe.woden.ircclient.ui.chat.transcript.line;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.app.api.PresenceEvent;
import cafe.woden.ircclient.app.api.PresenceKind;
import cafe.woden.ircclient.model.LogDirection;
import cafe.woden.ircclient.model.LogKind;
import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.fold.PresenceFoldComponent;
import cafe.woden.ircclient.ui.chat.render.ChatRichTextRenderer;
import cafe.woden.ircclient.ui.chat.transcript.LineMeta;
import javax.swing.text.AttributeSet;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import org.junit.jupiter.api.Test;

class ChatTranscriptPresenceFoldSupportTest {

  private final ChatStyles styles = new ChatStyles(null);
  private final ChatRichTextRenderer renderer = new ChatRichTextRenderer(null, null, styles, null);
  private final ChatTranscriptPresenceFoldSupport support =
      new ChatTranscriptPresenceFoldSupport(
          styles,
          renderer,
          null,
          ChatTranscriptLineMetaSupport::bind,
          ChatTranscriptLineMetaSupport::withExistingMeta,
          (base, match) -> new SimpleAttributeSet(base),
          ChatTranscriptPresenceFoldSupportTest::ensureAtLineStart,
          (ref, doc) -> 0);

  @Test
  void appendPresenceFoldsSecondConsecutiveEntryIntoPresenceComponent() {
    DefaultStyledDocument doc = new DefaultStyledDocument();
    ChatTranscriptPresenceFoldSupport.State state = new ChatTranscriptPresenceFoldSupport.State();
    TargetRef ref = new TargetRef("srv", "#chan");
    PresenceEvent join = PresenceEvent.join("alice");
    PresenceEvent part = PresenceEvent.part("bob", "bye");
    PresenceEvent quit = PresenceEvent.quit("carol", "gone");

    support.appendPresence(
        ref, doc, state, join, 1_000L, meta(ref, join, 1_000L), null, false, true);

    assertNull(lineComponent(doc, 0));
    assertEquals(1, lineCount(doc));

    support.appendPresence(
        ref, doc, state, part, 2_000L, meta(ref, part, 2_000L), null, false, true);

    PresenceFoldComponent folded =
        assertInstanceOf(PresenceFoldComponent.class, lineComponent(doc, 0));
    String tooltip = folded.getToolTipText();
    assertNotNull(tooltip);
    assertTrue(tooltip.contains("joined"));
    assertTrue(tooltip.contains("left"));
    assertEquals(1, lineCount(doc));

    support.appendPresence(
        ref, doc, state, quit, 3_000L, meta(ref, quit, 3_000L), null, false, true);

    assertSame(folded, lineComponent(doc, 0));
    assertEquals(1, lineCount(doc));
    assertTrue(folded.getToolTipText().contains("quit"));
  }

  @Test
  void appendPresenceLeavesSeparateLinesWhenFoldsAreDisabled() throws Exception {
    DefaultStyledDocument doc = new DefaultStyledDocument();
    ChatTranscriptPresenceFoldSupport.State state = new ChatTranscriptPresenceFoldSupport.State();
    TargetRef ref = new TargetRef("srv", "#chan");
    PresenceEvent join = PresenceEvent.join("alice");
    PresenceEvent part = PresenceEvent.part("bob", "bye");

    support.appendPresence(
        ref, doc, state, join, 1_000L, meta(ref, join, 1_000L), null, false, false);
    support.appendPresence(
        ref, doc, state, part, 2_000L, meta(ref, part, 2_000L), null, false, false);

    assertNull(lineComponent(doc, 0));
    assertNull(lineComponent(doc, 1));
    assertEquals(2, lineCount(doc));
    String text = doc.getText(0, doc.getLength());
    assertTrue(text.contains(join.displayText()));
    assertTrue(text.contains(part.displayText()));
  }

  private static LineMeta meta(TargetRef ref, PresenceEvent event, long epochMs) {
    String fromNick = event.kind() == PresenceKind.NICK ? event.oldNick() : event.nick();
    return ChatTranscriptLineMetaSupport.create(
        ref, LogKind.PRESENCE, LogDirection.SYSTEM, fromNick, epochMs, event);
  }

  private static void ensureAtLineStart(StyledDocument doc) {
    if (doc == null) {
      return;
    }
    int len = doc.getLength();
    if (len <= 0) {
      return;
    }
    try {
      if (!"\n".equals(doc.getText(len - 1, 1))) {
        doc.insertString(len, "\n", new SimpleAttributeSet());
      }
    } catch (Exception ignored) {
    }
  }

  private static java.awt.Component lineComponent(StyledDocument doc, int lineIndex) {
    Element root = doc.getDefaultRootElement();
    if (root == null || lineIndex < 0 || lineIndex >= root.getElementCount()) {
      return null;
    }
    Element line = root.getElement(lineIndex);
    if (line == null) {
      return null;
    }
    int start = Math.max(0, line.getStartOffset());
    if (start >= doc.getLength()) {
      return null;
    }
    AttributeSet attrs = doc.getCharacterElement(start).getAttributes();
    return StyleConstants.getComponent(attrs);
  }

  private static int lineCount(StyledDocument doc) {
    try {
      return (int) doc.getText(0, doc.getLength()).chars().filter(ch -> ch == '\n').count();
    } catch (Exception ignored) {
      return 0;
    }
  }
}
