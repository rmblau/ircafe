package cafe.woden.ircclient.ui.chat.transcript.line;

import static cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageMetadataSupport.normalizeMessageId;
import static cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageMetadataSupport.normalizePendingId;

import cafe.woden.ircclient.ui.chat.ChatStyles;
import java.awt.Component;
import java.util.Objects;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/** Shared transcript document scan and low-level mutation helpers. */
public final class ChatTranscriptDocumentSupport {

  public record MessageLine(String targetMessageId, int lineStart, AttributeSet attrs) {}

  private ChatTranscriptDocumentSupport() {}

  public static int findLineStartByMessageId(StyledDocument doc, String messageId) {
    if (doc == null) return -1;
    String want = normalizeMessageId(messageId);
    if (want.isEmpty()) return -1;
    try {
      Element root = doc.getDefaultRootElement();
      if (root == null) return -1;
      int len = doc.getLength();
      for (int i = 0; i < root.getElementCount(); i++) {
        Element line = root.getElement(i);
        if (line == null) continue;
        int start = Math.max(0, line.getStartOffset());
        if (start >= len) continue;
        int end = Math.max(start, Math.min(line.getEndOffset(), len));
        for (int p = start; p < end; p++) {
          AttributeSet attrs = doc.getCharacterElement(p).getAttributes();
          String got = Objects.toString(attrs.getAttribute(ChatStyles.ATTR_META_MSGID), "").trim();
          if (want.equals(got)) return start;
        }
      }
    } catch (Exception ignored) {
    }
    return -1;
  }

  public static int lineEndOffsetForLineStart(StyledDocument doc, int lineStart) {
    if (doc == null) return Math.max(0, lineStart);
    try {
      Element root = doc.getDefaultRootElement();
      if (root == null) return Math.max(0, lineStart);
      int idx = root.getElementIndex(Math.max(0, lineStart));
      Element line = root.getElement(idx);
      if (line == null) return Math.max(0, lineStart);
      int end = line.getEndOffset();
      return Math.max(0, Math.min(end, doc.getLength()));
    } catch (Exception ignored) {
      return Math.max(0, lineStart);
    }
  }

  public static MessageLine findMessageLine(StyledDocument doc, String messageId) {
    if (doc == null) return null;
    String targetMsgId = normalizeMessageId(messageId);
    if (targetMsgId.isEmpty()) return null;

    int lineStart = findLineStartByMessageId(doc, targetMsgId);
    if (lineStart < 0) return null;

    AttributeSet attrs = attributesAt(doc, lineStart);
    return attrs == null ? null : new MessageLine(targetMsgId, lineStart, attrs);
  }

  public static int findAuxiliaryLineStartByMessageId(
      StyledDocument doc, String messageId, String auxiliaryRowKind) {
    if (doc == null) return -1;
    String wantMessageId = normalizeMessageId(messageId);
    String wantAuxiliaryRowKind = Objects.toString(auxiliaryRowKind, "").trim();
    if (wantMessageId.isEmpty() || wantAuxiliaryRowKind.isEmpty()) return -1;
    try {
      Element root = doc.getDefaultRootElement();
      if (root == null) return -1;
      int len = doc.getLength();
      for (int i = 0; i < root.getElementCount(); i++) {
        Element line = root.getElement(i);
        if (line == null) continue;
        int start = Math.max(0, line.getStartOffset());
        if (start >= len) continue;
        AttributeSet attrs = doc.getCharacterElement(start).getAttributes();
        String gotMessageId =
            Objects.toString(attrs.getAttribute(ChatStyles.ATTR_META_MSGID), "").trim();
        String gotAuxiliaryRowKind =
            Objects.toString(attrs.getAttribute(ChatStyles.ATTR_META_AUX_ROW_KIND), "").trim();
        if (wantMessageId.equals(gotMessageId)
            && wantAuxiliaryRowKind.equals(gotAuxiliaryRowKind)) {
          return start;
        }
      }
    } catch (Exception ignored) {
    }
    return -1;
  }

  public static int nextLineStart(StyledDocument doc, int lineStart) {
    if (doc == null) return Math.max(0, lineStart);
    try {
      Element root = doc.getDefaultRootElement();
      if (root == null) return lineEndOffsetForLineStart(doc, lineStart);
      int len = doc.getLength();
      int idx = root.getElementIndex(Math.max(0, Math.min(lineStart, len)));
      if (idx + 1 >= root.getElementCount()) {
        return len;
      }
      Element next = root.getElement(idx + 1);
      if (next == null) return lineEndOffsetForLineStart(doc, lineStart);
      return Math.max(0, Math.min(next.getStartOffset(), len));
    } catch (Exception ignored) {
      return lineEndOffsetForLineStart(doc, lineStart);
    }
  }

  public static int findLineStartByPendingId(StyledDocument doc, String pendingId) {
    if (doc == null) return -1;
    String want = normalizePendingId(pendingId);
    if (want.isEmpty()) return -1;
    try {
      Element root = doc.getDefaultRootElement();
      if (root == null) return -1;
      int len = doc.getLength();
      for (int i = 0; i < root.getElementCount(); i++) {
        Element line = root.getElement(i);
        if (line == null) continue;
        int start = Math.max(0, line.getStartOffset());
        int end = Math.max(start, Math.min(line.getEndOffset(), len));
        if (start >= end) continue;
        for (int p = start; p < end; p++) {
          AttributeSet attrs = doc.getCharacterElement(p).getAttributes();
          String got =
              Objects.toString(attrs.getAttribute(ChatStyles.ATTR_META_PENDING_ID), "").trim();
          if (want.equals(got)) return start;
        }
      }
    } catch (Exception ignored) {
    }
    return -1;
  }

  static int findInlineComponentOffset(StyledDocument doc, int start, int end, Component expected) {
    if (doc == null || expected == null) return -1;
    int len = doc.getLength();
    if (len <= 0) return -1;

    int s = Math.max(0, Math.min(start, len - 1));
    int e = Math.max(0, Math.min(end, len - 1));
    if (e < s) {
      int tmp = s;
      s = e;
      e = tmp;
    }
    for (int i = s; i <= e; i++) {
      try {
        Element el = doc.getCharacterElement(i);
        if (el == null) continue;
        AttributeSet attrs = el.getAttributes();
        Object component = attrs != null ? StyleConstants.getComponent(attrs) : null;
        if (component == expected) return i;
      } catch (Exception ignored) {
      }
    }
    return -1;
  }

  public static void markLineRangeRedacted(StyledDocument doc, int lineStart) {
    if (doc == null || lineStart < 0) return;
    int lineEnd = lineEndOffsetForLineStart(doc, lineStart);
    int len = Math.max(0, lineEnd - lineStart);
    if (len <= 0) return;
    SimpleAttributeSet attrs = new SimpleAttributeSet();
    attrs.addAttribute(ChatStyles.ATTR_META_REDACTED, Boolean.TRUE);
    try {
      doc.setCharacterAttributes(lineStart, len, attrs, false);
    } catch (Exception ignored) {
    }
  }

  private static AttributeSet attributesAt(StyledDocument doc, int offset) {
    if (doc == null) return null;
    try {
      return doc.getCharacterElement(
              Math.max(0, Math.min(offset, Math.max(0, doc.getLength() - 1))))
          .getAttributes();
    } catch (Exception ignored) {
      return null;
    }
  }
}
