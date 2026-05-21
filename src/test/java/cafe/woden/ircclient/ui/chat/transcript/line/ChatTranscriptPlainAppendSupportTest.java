package cafe.woden.ircclient.ui.chat.transcript.line;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyledDocument;
import org.junit.jupiter.api.Test;

class ChatTranscriptPlainAppendSupportTest {

  @Test
  void appendPlainEnsuresTargetBreaksPresenceRunWritesTextAndEnforcesLineCap() throws Exception {
    TargetRef ref = new TargetRef("srv", "#chan");
    StyledDocument doc = new DefaultStyledDocument();
    Map<TargetRef, StyledDocument> docs = new HashMap<>();
    AtomicInteger targetEnsures = new AtomicInteger();
    AtomicInteger presenceBreaks = new AtomicInteger();
    AtomicInteger lineCapCalls = new AtomicInteger();
    ChatTranscriptPlainAppendSupport.Context context =
        new ChatTranscriptPlainAppendSupport.Context(
            docs,
            new ChatStyles(null),
            target -> {
              assertEquals(ref, target);
              targetEnsures.incrementAndGet();
              docs.put(target, doc);
            },
            target -> {
              assertEquals(ref, target);
              presenceBreaks.incrementAndGet();
            },
            (target, targetDoc) -> {
              assertEquals(ref, target);
              assertSame(doc, targetDoc);
              lineCapCalls.incrementAndGet();
              return 0;
            });

    ChatTranscriptPlainAppendSupport.appendPlain(context, ref, "plain text");

    assertEquals("plain text", doc.getText(0, doc.getLength()));
    assertEquals(1, targetEnsures.get());
    assertEquals(1, presenceBreaks.get());
    assertEquals(1, lineCapCalls.get());
  }
}
