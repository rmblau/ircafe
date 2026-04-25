package cafe.woden.ircclient.ui.chat.transcript.line;

import cafe.woden.ircclient.ui.chat.ChatStyles;
import java.util.Objects;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;

public final class ChatTranscriptDocumentLineSupport {

  private final ChatStyles styles;

  public ChatTranscriptDocumentLineSupport(ChatStyles styles) {
    this.styles = Objects.requireNonNull(styles, "styles");
  }

  public void ensureAtLineStart(StyledDocument doc) {
    if (doc == null) {
      return;
    }
    int len = doc.getLength();
    if (len <= 0) {
      return;
    }
    try {
      String last = doc.getText(len - 1, 1);
      if (!"\n".equals(last)) {
        AttributeSet lastAttrs = null;
        try {
          lastAttrs = doc.getCharacterElement(Math.max(0, len - 1)).getAttributes();
        } catch (Exception ignored) {
          lastAttrs = null;
        }
        doc.insertString(
            len,
            "\n",
            ChatTranscriptLineMetaSupport.withExistingMeta(styles.timestamp(), lastAttrs));
      }
    } catch (Exception ignored) {
    }
  }

  public int normalizeInsertAtLineStart(StyledDocument doc, int insertAt) {
    if (doc == null) {
      return 0;
    }
    int len = doc.getLength();
    if (len <= 0) {
      return 0;
    }
    int pos = Math.max(0, Math.min(insertAt, len));
    if (pos <= 0 || pos >= len) {
      return pos;
    }

    try {
      Element root = doc.getDefaultRootElement();
      if (root == null) {
        return pos;
      }
      int line = root.getElementIndex(pos);
      Element element = root.getElement(line);
      if (element == null) {
        return pos;
      }
      int start = element.getStartOffset();
      return Math.max(0, Math.min(start, len));
    } catch (Exception ignored) {
      return pos;
    }
  }

  public int ensureAtLineStartForInsert(StyledDocument doc, int pos) {
    if (doc == null) {
      return Math.max(0, pos);
    }
    int len = doc.getLength();
    int clamped = Math.max(0, Math.min(pos, len));
    if (clamped <= 0) {
      return clamped;
    }
    try {
      String prev = doc.getText(clamped - 1, 1);
      if (!"\n".equals(prev)) {
        AttributeSet prevAttrs = null;
        try {
          prevAttrs = doc.getCharacterElement(Math.max(0, clamped - 1)).getAttributes();
        } catch (Exception ignored) {
          prevAttrs = null;
        }
        doc.insertString(
            clamped,
            "\n",
            ChatTranscriptLineMetaSupport.withExistingMeta(styles.timestamp(), prevAttrs));
        return clamped + 1;
      }
    } catch (Exception ignored) {
    }
    return clamped;
  }
}
