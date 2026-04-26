package cafe.woden.ircclient.ui.chat.transcript.line;

import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.transcript.line.LineMeta;
import java.awt.Component;
import java.util.Map;
import java.util.Objects;
import javax.swing.SwingUtilities;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledDocument;

/** Coordinates outbound delivery indicator insertion and EDT-safe component removal. */
public final class ChatTranscriptOutgoingDeliverySupport {

  private final Map<TargetRef, StyledDocument> docs;
  private final Object lock;

  public ChatTranscriptOutgoingDeliverySupport(Map<TargetRef, StyledDocument> docs, Object lock) {
    this.docs = Objects.requireNonNull(docs, "docs");
    this.lock = Objects.requireNonNull(lock, "lock");
  }

  public void insertConfirmedDot(
      TargetRef ref, int after, SimpleAttributeSet messageStyle, LineMeta meta) {
    try {
      StyledDocument doc = docs.get(ref);
      if (doc == null) {
        return;
      }
      SimpleAttributeSet attrs = new SimpleAttributeSet(messageStyle);
      attrs = ChatTranscriptLineMetaSupport.bind(attrs, meta);
      ChatTranscriptDeliveryIndicatorSupport.insertConfirmedDot(
          doc, after, attrs, component -> removeInlineComponentNear(doc, component));
    } catch (Exception ignored) {
    }
  }

  private boolean removeInlineComponentNear(StyledDocument doc, Component expected) {
    if (doc == null || expected == null) return false;
    if (!SwingUtilities.isEventDispatchThread()) {
      final boolean[] ok = new boolean[] {false};
      try {
        SwingUtilities.invokeAndWait(() -> ok[0] = removeInlineComponentNear(doc, expected));
      } catch (Exception ignored) {
        return false;
      }
      return ok[0];
    }

    synchronized (lock) {
      return ChatTranscriptDeliveryIndicatorSupport.removeInlineComponent(doc, expected);
    }
  }
}
