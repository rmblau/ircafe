package cafe.woden.ircclient.ui.chat.transcript.line;

import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptLineCapSupport;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import javax.swing.text.StyledDocument;

public final class ChatTranscriptManualPreviewFlowSupport {

  @FunctionalInterface
  public interface PresenceBlockShiftHandler {
    void shift(TargetRef ref, int insertAt, int delta);
  }

  public record Context(
      Map<TargetRef, StyledDocument> docs,
      Consumer<TargetRef> ensureTargetExists,
      ChatTranscriptManualPreviewSupport manualPreviewSupport,
      PresenceBlockShiftHandler presenceBlockShiftHandler,
      ChatTranscriptLineCapSupport lineCapSupport) {

    public Context {
      Objects.requireNonNull(docs, "docs");
      Objects.requireNonNull(ensureTargetExists, "ensureTargetExists");
      Objects.requireNonNull(manualPreviewSupport, "manualPreviewSupport");
      Objects.requireNonNull(presenceBlockShiftHandler, "presenceBlockShiftHandler");
      Objects.requireNonNull(lineCapSupport, "lineCapSupport");
    }
  }

  public boolean insertManualPreviewAt(
      Context context, TargetRef ref, int insertAt, String rawUrl) {
    if (context == null || ref == null) return false;

    context.ensureTargetExists().accept(ref);
    StyledDocument doc = context.docs().get(ref);
    if (doc == null) return false;

    int pos = Math.max(0, Math.min(insertAt, doc.getLength()));
    int beforeLen = doc.getLength();
    boolean inserted = context.manualPreviewSupport().insertManualPreviewAt(ref, doc, pos, rawUrl);
    if (!inserted) return false;

    int delta = doc.getLength() - beforeLen;
    if (delta != 0) {
      context.presenceBlockShiftHandler().shift(ref, pos, delta);
    }
    context.lineCapSupport().enforceTranscriptLineCap(ref, doc);
    return true;
  }
}
