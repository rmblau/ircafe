package cafe.woden.ircclient.ui.chat.transcript.runtime;

import cafe.woden.ircclient.model.TargetRef;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;

public final class ChatTranscriptLineCapSupport {

  private final IntSupplier maxLinesSupplier;
  private final Consumer<TargetRef> headTrimReset;
  private final Consumer<TargetRef> postTrimHandler;

  public ChatTranscriptLineCapSupport(
      IntSupplier maxLinesSupplier,
      Consumer<TargetRef> headTrimReset,
      Consumer<TargetRef> postTrimHandler) {
    this.maxLinesSupplier = Objects.requireNonNull(maxLinesSupplier, "maxLinesSupplier");
    this.headTrimReset = Objects.requireNonNull(headTrimReset, "headTrimReset");
    this.postTrimHandler = Objects.requireNonNull(postTrimHandler, "postTrimHandler");
  }

  public int enforceTranscriptLineCap(TargetRef ref, StyledDocument doc) {
    if (ref == null || doc == null) {
      return 0;
    }

    int maxLines = maxLinesSupplier.getAsInt();
    if (maxLines <= 0) {
      return 0;
    }

    int lineCount = logicalLineCount(doc);
    if (lineCount <= maxLines) {
      return 0;
    }

    int trimLines = lineCount - maxLines;
    try {
      Element root = doc.getDefaultRootElement();
      if (root == null || trimLines <= 0) {
        return 0;
      }
      int idx = Math.min(root.getElementCount() - 1, trimLines - 1);
      if (idx < 0) {
        return 0;
      }
      Element lastTrimmed = root.getElement(idx);
      if (lastTrimmed == null) {
        return 0;
      }
      int removeLen = Math.max(0, Math.min(lastTrimmed.getEndOffset(), doc.getLength()));
      if (removeLen <= 0) {
        return 0;
      }

      doc.remove(0, removeLen);
      headTrimReset.accept(ref);
      postTrimHandler.accept(ref);
      return removeLen;
    } catch (Exception ignored) {
      return 0;
    }
  }

  public static int logicalLineCount(StyledDocument doc) {
    if (doc == null) {
      return 0;
    }
    try {
      Element root = doc.getDefaultRootElement();
      if (root == null) {
        return 0;
      }
      int count = Math.max(0, root.getElementCount());
      int len = doc.getLength();
      if (count > 0 && len > 0) {
        String last = doc.getText(len - 1, 1);
        if ("\n".equals(last)) {
          count = Math.max(0, count - 1);
        }
      }
      return count;
    } catch (Exception ignored) {
      return 0;
    }
  }
}
