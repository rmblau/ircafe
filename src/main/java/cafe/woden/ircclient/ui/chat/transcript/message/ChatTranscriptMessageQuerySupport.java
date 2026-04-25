package cafe.woden.ircclient.ui.chat.transcript.message;

import static cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageMetadataSupport.normalizeMessageId;

import cafe.woden.ircclient.model.LogDirection;
import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStore;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptDocumentSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptState;
import cafe.woden.ircclient.ui.chat.transcript.style.ChatTranscriptAttrSupport;
import java.util.Map;
import java.util.Objects;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyledDocument;

/** Read-side lookup helpers for transcript message metadata and cached previews. */
public final class ChatTranscriptMessageQuerySupport {

  public record Context(
      Map<TargetRef, StyledDocument> docs,
      Map<TargetRef, ChatTranscriptState> stateByTarget,
      ChatTranscriptMessageCatalogSupport messageCatalogSupport) {
    public Context {
      Objects.requireNonNull(docs, "docs");
      Objects.requireNonNull(stateByTarget, "stateByTarget");
      Objects.requireNonNull(messageCatalogSupport, "messageCatalogSupport");
    }
  }

  public int messageOffsetById(Context context, TargetRef ref, String messageId) {
    if (context == null || ref == null) return -1;
    String msgId = normalizeMessageId(messageId);
    if (msgId.isEmpty()) return -1;
    StyledDocument doc = context.docs().get(ref);
    if (doc == null) return -1;
    return ChatTranscriptDocumentSupport.findLineStartByMessageId(doc, msgId);
  }

  public String messagePreviewById(Context context, TargetRef ref, String messageId) {
    if (context == null || ref == null) return "";
    String msgId = normalizeMessageId(messageId);
    if (msgId.isEmpty()) return "";
    ChatTranscriptState st = context.stateByTarget().get(ref);
    if (st == null) return "";
    return context.messageCatalogSupport().previewForMessageId(st.messageCatalog(), msgId);
  }

  public ChatTranscriptStore.RedactedMessageContent redactedOriginalById(
      Context context, TargetRef ref, String messageId) {
    if (context == null || ref == null) return null;
    String msgId = normalizeMessageId(messageId);
    if (msgId.isEmpty()) return null;
    ChatTranscriptState st = context.stateByTarget().get(ref);
    if (st == null) return null;
    return context.messageCatalogSupport().redactedOriginalById(st.messageCatalog(), msgId);
  }

  public boolean isOwnMessage(Context context, TargetRef ref, String messageId) {
    if (context == null || ref == null) return false;
    String msgId = normalizeMessageId(messageId);
    if (msgId.isEmpty()) return false;
    StyledDocument doc = context.docs().get(ref);
    if (doc == null) return false;

    int lineStart = ChatTranscriptDocumentSupport.findLineStartByMessageId(doc, msgId);
    if (lineStart < 0) return false;

    try {
      int len = doc.getLength();
      if (len <= 0) return false;
      int safePos = Math.max(0, Math.min(lineStart, len - 1));
      AttributeSet attrs = doc.getCharacterElement(safePos).getAttributes();
      if (attrs == null) return false;
      if (Boolean.TRUE.equals(attrs.getAttribute(ChatStyles.ATTR_OUTGOING))) return true;
      return ChatTranscriptAttrSupport.logDirectionFromAttrs(attrs) == LogDirection.OUT;
    } catch (Exception ignored) {
      return false;
    }
  }
}
