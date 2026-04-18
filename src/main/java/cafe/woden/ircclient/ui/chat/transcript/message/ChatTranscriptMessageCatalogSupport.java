package cafe.woden.ircclient.ui.chat.transcript;

import static cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptMessageMetadataSupport.normalizeMessageId;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import javax.swing.text.AttributeSet;

/** Owns per-target preview/current-message/redaction transcript caches. */
final class ChatTranscriptMessageCatalogSupport {

  static final class State {
    private final Map<String, String> messagePreviewByMsgId;
    private final Map<String, ChatTranscriptStore.MessageContentSnapshot>
        currentMessageContentByMsgId;
    private final Map<String, ChatTranscriptStore.RedactedMessageContent> redactedOriginalByMsgId;

    private State(int replyPreviewCacheLimit, int redactedMessageCacheLimit) {
      this.messagePreviewByMsgId =
          ChatTranscriptReplyPreviewSupport.createBoundedReplyPreviewCache(replyPreviewCacheLimit);
      this.currentMessageContentByMsgId = createBoundedCache(replyPreviewCacheLimit);
      this.redactedOriginalByMsgId = createBoundedCache(redactedMessageCacheLimit);
    }
  }

  private final ChatTranscriptMessageStateSupport.Context messageStateSupportContext;

  ChatTranscriptMessageCatalogSupport(
      ChatTranscriptMessageStateSupport.Context messageStateSupportContext) {
    this.messageStateSupportContext =
        Objects.requireNonNull(messageStateSupportContext, "messageStateSupportContext");
  }

  State createState(int replyPreviewCacheLimit, int redactedMessageCacheLimit) {
    return new State(replyPreviewCacheLimit, redactedMessageCacheLimit);
  }

  String previewForMessageId(State state, String messageId) {
    return ChatTranscriptReplyPreviewSupport.previewForMessageId(
        state == null ? null : state.messagePreviewByMsgId, messageId);
  }

  ChatTranscriptStore.RedactedMessageContent redactedOriginalById(State state, String messageId) {
    if (state == null) return null;
    String msgId = normalizeMessageId(messageId);
    if (msgId.isEmpty()) return null;
    return state.redactedOriginalByMsgId.get(msgId);
  }

  void rememberMessagePreview(
      State state, LineMeta meta, String renderedFrom, String renderedText) {
    if (state == null) return;
    ChatTranscriptMessageStateSupport.rememberMessagePreview(
        messageStateSupportContext, state.messagePreviewByMsgId, meta, renderedFrom, renderedText);
  }

  void recordInsertedMessage(State state, LineMeta meta, String renderedFrom, String renderedText) {
    if (state == null) return;
    rememberMessagePreview(state, meta, renderedFrom, renderedText);
    ChatTranscriptMessageStateSupport.rememberCurrentMessageContent(
        state.currentMessageContentByMsgId, meta, renderedFrom, renderedText);
  }

  void rememberEditedCurrentMessageContent(
      State state, String targetMessageId, AttributeSet existingAttrs, String renderedEditedText) {
    if (state == null) return;
    ChatTranscriptMessageStateSupport.rememberEditedCurrentMessageContent(
        state.currentMessageContentByMsgId, targetMessageId, existingAttrs, renderedEditedText);
  }

  void rememberRedactedOriginal(
      State state,
      String targetMessageId,
      AttributeSet existingAttrs,
      String redactedBy,
      long redactedAtEpochMs) {
    if (state == null) return;
    ChatTranscriptMessageStateSupport.rememberRedactedOriginal(
        messageStateSupportContext,
        state.currentMessageContentByMsgId,
        state.redactedOriginalByMsgId,
        targetMessageId,
        existingAttrs,
        redactedBy,
        redactedAtEpochMs);
  }

  void clearCurrentMessageContent(State state, String targetMessageId) {
    if (state == null) return;
    String msgId = normalizeMessageId(targetMessageId);
    if (msgId.isEmpty()) return;
    state.currentMessageContentByMsgId.remove(msgId);
  }

  private static <K, V> LinkedHashMap<K, V> createBoundedCache(int maxEntries) {
    return new LinkedHashMap<>() {
      @Override
      protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > maxEntries;
      }
    };
  }
}
