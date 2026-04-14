package cafe.woden.ircclient.ui.chat.transcript;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.ui.chat.fold.SpoilerMessageComponent;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import org.junit.jupiter.api.Test;

class ChatTranscriptSpoilerLineSupportTest {

  @Test
  void writeLineAtInsertsSpoilerComponentAndNewlineAtOffset() throws Exception {
    DefaultStyledDocument doc = new DefaultStyledDocument();
    TestSpoilerMessageComponent component = new TestSpoilerMessageComponent();

    ChatTranscriptSpoilerLineSupport.WriteResult result =
        ChatTranscriptSpoilerLineSupport.writeLineAt(
            doc,
            0,
            component,
            new SimpleAttributeSet(),
            new SimpleAttributeSet(),
            pos -> () -> true);

    assertEquals(" \n", doc.getText(0, doc.getLength()));
    assertSame(component, StyleConstants.getComponent(doc.getCharacterElement(0).getAttributes()));
    assertEquals(1, result.lineEndOffset());
    assertEquals(2, result.nextOffset());
  }

  @Test
  void writeLineAtBindsRevealHandlerToInsertedSpoilerPosition() throws Exception {
    DefaultStyledDocument doc = new DefaultStyledDocument();
    doc.insertString(0, "tail\n", new SimpleAttributeSet());
    TestSpoilerMessageComponent component = new TestSpoilerMessageComponent();
    AtomicInteger revealCalls = new AtomicInteger();

    ChatTranscriptSpoilerLineSupport.writeLineAt(
        doc,
        0,
        component,
        new SimpleAttributeSet(),
        new SimpleAttributeSet(),
        pos ->
            () -> {
              revealCalls.incrementAndGet();
              return pos.getOffset() == 0;
            });

    assertEquals(" \ntail\n", doc.getText(0, doc.getLength()));
    assertTrue(component.revealHandler != null && component.revealHandler.getAsBoolean());
    assertEquals(1, revealCalls.get());
  }

  private static final class TestSpoilerMessageComponent extends SpoilerMessageComponent {

    private BooleanSupplier revealHandler;

    private TestSpoilerMessageComponent() {
      super("", "");
    }

    @Override
    public void setOnReveal(BooleanSupplier onReveal) {
      revealHandler = onReveal;
      super.setOnReveal(onReveal);
    }
  }
}
