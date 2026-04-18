package cafe.woden.ircclient.ui.chat.transcript;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import cafe.woden.ircclient.model.TargetRef;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import org.junit.jupiter.api.Test;

class ChatTranscriptLineCapSupportTest {

  @Test
  void logicalLineCountIgnoresTrailingNewline() throws Exception {
    DefaultStyledDocument doc = new DefaultStyledDocument();
    doc.insertString(0, "one\ntwo\n", new SimpleAttributeSet());

    assertEquals(2, ChatTranscriptLineCapSupport.logicalLineCount(doc));
  }

  @Test
  void enforceTranscriptLineCapTrimsHeadAndNotifiesCallbacks() throws Exception {
    AtomicInteger resets = new AtomicInteger();
    AtomicInteger postTrimCalls = new AtomicInteger();
    AtomicReference<TargetRef> resetRef = new AtomicReference<>();
    AtomicReference<TargetRef> postRef = new AtomicReference<>();
    ChatTranscriptLineCapSupport support =
        new ChatTranscriptLineCapSupport(
            () -> 2,
            ref -> {
              resetRef.set(ref);
              resets.incrementAndGet();
            },
            ref -> {
              postRef.set(ref);
              postTrimCalls.incrementAndGet();
            });
    DefaultStyledDocument doc = new DefaultStyledDocument();
    doc.insertString(0, "one\ntwo\nthree\n", new SimpleAttributeSet());
    TargetRef ref = new TargetRef("srv", "#chan");

    int removed = support.enforceTranscriptLineCap(ref, doc);

    assertEquals(4, removed);
    assertEquals("two\nthree\n", doc.getText(0, doc.getLength()));
    assertEquals(1, resets.get());
    assertEquals(1, postTrimCalls.get());
    assertSame(ref, resetRef.get());
    assertSame(ref, postRef.get());
  }

  @Test
  void enforceTranscriptLineCapDoesNothingWhenDocumentAlreadyFits() throws Exception {
    AtomicInteger resets = new AtomicInteger();
    ChatTranscriptLineCapSupport support =
        new ChatTranscriptLineCapSupport(() -> 3, ref -> resets.incrementAndGet(), ref -> {});
    DefaultStyledDocument doc = new DefaultStyledDocument();
    doc.insertString(0, "one\ntwo\n", new SimpleAttributeSet());

    int removed = support.enforceTranscriptLineCap(new TargetRef("srv", "#chan"), doc);

    assertEquals(0, removed);
    assertEquals("one\ntwo\n", doc.getText(0, doc.getLength()));
    assertEquals(0, resets.get());
  }
}
