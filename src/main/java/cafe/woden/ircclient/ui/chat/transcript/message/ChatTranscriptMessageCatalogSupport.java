package cafe.woden.ircclient.ui.chat.transcript.message;

import static cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageMetadataSupport.normalizeMessageId;

import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptReplyPreviewSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.LineMeta;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import javax.swing.text.AttributeSet;

/** Owns per-target preview/current-message/redaction transcript caches. */
public final class ChatTranscriptMessageCatalogSupport {

  public static final class State {
    private final Map<String, String> messagePreviewByMsgId;
    private final Map<String, MessageContentSnapshot> currentMessageContentByMsgId;
    private final Map<String, RedactedMessageContent> redactedOriginalByMsgId;

    private State(int replyPreviewCacheLimit, int redactedMessageCacheLimit) {
      this.messagePreviewByMsgId =
          ChatTranscriptReplyPreviewSupport.createBoundedReplyPreviewCache(replyPreviewCacheLimit);
      this.currentMessageContentByMsgId = createBoundedCache(replyPreviewCacheLimit);
      this.redactedOriginalByMsgId = createBoundedCache(redactedMessageCacheLimit);
    }
  }

  private final ChatTranscriptMessageStateSupport.Context messageStateSupportContext;

  public ChatTranscriptMessageCatalogSupport(
      ChatTranscriptMessageStateSupport.Context messageStateSupportContext) {
    this.messageStateSupportContext =
        Objects.requireNonNull(messageStateSupportContext, "messageStateSupportContext");
  }

  public State createState(int replyPreviewCacheLimit, int redactedMessageCacheLimit) {
    return new State(replyPreviewCacheLimit, redactedMessageCacheLimit);
  }

  public String previewForMessageId(State state, String messageId) {
    return ChatTranscriptReplyPreviewSupport.previewForMessageId(
        state == null ? null : state.messagePreviewByMsgId, messageId);
  }

  public RedactedMessageContent redactedOriginalById(State state, String messageId) {
    if (state == null) return null;
    String msgId = normalizeMessageId(messageId);
    if (msgId.isEmpty()) return null;
    return state.redactedOriginalByMsgId.get(msgId);
  }

  public MessageTranslationSource translationSourceById(State state, String messageId) {
    if (state == null) return null;
    String msgId = normalizeMessageId(messageId);
    if (msgId.isEmpty()) return null;
    MessageContentSnapshot snapshot = state.currentMessageContentByMsgId.get(msgId);
    if (snapshot == null) return null;
    return new MessageTranslationSource(
        msgId, snapshot.kind(), snapshot.fromNick(), snapshot.renderedText(), snapshot.epochMs());
  }

  public void rememberMessagePreview(
      State state, LineMeta meta, String renderedFrom, String renderedText) {
    if (state == null) return;
    ChatTranscriptMessageStateSupport.rememberMessagePreview(
        messageStateSupportContext, state.messagePreviewByMsgId, meta, renderedFrom, renderedText);
  }

  public void recordInsertedMessage(
      State state, LineMeta meta, String renderedFrom, String renderedText) {
    if (state == null) return;
    rememberMessagePreview(state, meta, renderedFrom, renderedText);
    ChatTranscriptMessageStateSupport.rememberCurrentMessageContent(
        state.currentMessageContentByMsgId, meta, renderedFrom, renderedText);
  }

  public void rememberEditedCurrentMessageContent(
      State state, String targetMessageId, AttributeSet existingAttrs, String renderedEditedText) {
    if (state == null) return;
    ChatTranscriptMessageStateSupport.rememberEditedCurrentMessageContent(
        state.currentMessageContentByMsgId, targetMessageId, existingAttrs, renderedEditedText);
  }

  public void rememberRedactedOriginal(
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

  public void clearCurrentMessageContent(State state, String targetMessageId) {
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
