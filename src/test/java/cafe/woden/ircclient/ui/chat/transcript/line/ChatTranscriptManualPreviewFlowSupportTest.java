package cafe.woden.ircclient.ui.chat.transcript.line;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptLineCapSupport;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyledDocument;
import org.junit.jupiter.api.Test;

class ChatTranscriptManualPreviewFlowSupportTest {

  @Test
  void insertManualPreviewShiftsPresenceAndEnforcesLineCapAfterInsert() throws Exception {
    ChatTranscriptManualPreviewSupport manualPreviewSupport =
        mock(ChatTranscriptManualPreviewSupport.class);
    ChatTranscriptLineCapSupport lineCapSupport = mock(ChatTranscriptLineCapSupport.class);
    ChatTranscriptManualPreviewFlowSupport support = new ChatTranscriptManualPreviewFlowSupport();
    TargetRef ref = new TargetRef("srv", "#chan");
    StyledDocument doc = new DefaultStyledDocument();
    doc.insertString(0, "hello", null);
    Map<TargetRef, StyledDocument> docs = new HashMap<>();
    docs.put(ref, doc);
    AtomicBoolean ensured = new AtomicBoolean(false);
    AtomicReference<String> shiftCapture = new AtomicReference<>();
    ChatTranscriptManualPreviewFlowSupport.Context context =
        new ChatTranscriptManualPreviewFlowSupport.Context(
            docs,
            target -> {
              if (ref.equals(target)) {
                ensured.set(true);
              }
            },
            manualPreviewSupport,
            (target, insertAt, delta) -> shiftCapture.set(target + ":" + insertAt + ":" + delta),
            lineCapSupport);
    when(manualPreviewSupport.insertManualPreviewAt(ref, doc, 3, "https://example.test"))
        .thenAnswer(
            invocation -> {
              doc.insertString(3, "[preview]", null);
              return true;
            });

    boolean inserted = support.insertManualPreviewAt(context, ref, 3, "https://example.test");

    assertTrue(inserted);
    assertTrue(ensured.get());
    org.junit.jupiter.api.Assertions.assertEquals(ref + ":3:9", shiftCapture.get());
    verify(lineCapSupport).enforceTranscriptLineCap(ref, doc);
  }

  @Test
  void insertManualPreviewReturnsFalseWhenPreviewSupportRejectsInsert() {
    ChatTranscriptManualPreviewSupport manualPreviewSupport =
        mock(ChatTranscriptManualPreviewSupport.class);
    ChatTranscriptLineCapSupport lineCapSupport = mock(ChatTranscriptLineCapSupport.class);
    ChatTranscriptManualPreviewFlowSupport support = new ChatTranscriptManualPreviewFlowSupport();
    TargetRef ref = new TargetRef("srv", "#chan");
    StyledDocument doc = new DefaultStyledDocument();
    ChatTranscriptManualPreviewFlowSupport.Context context =
        new ChatTranscriptManualPreviewFlowSupport.Context(
            Map.of(ref, doc),
            target -> {},
            manualPreviewSupport,
            (target, insertAt, delta) -> {},
            lineCapSupport);
    when(manualPreviewSupport.insertManualPreviewAt(ref, doc, 0, "https://example.test"))
        .thenReturn(false);

    assertFalse(support.insertManualPreviewAt(context, ref, 0, "https://example.test"));
  }
}
