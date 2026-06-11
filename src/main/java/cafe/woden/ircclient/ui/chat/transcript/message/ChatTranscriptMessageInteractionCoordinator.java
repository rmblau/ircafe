package cafe.woden.ircclient.ui.chat.transcript.message;

import static cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageMetadataSupport.normalizeMessageId;

import cafe.woden.ircclient.app.api.MessageTranslation;
import cafe.woden.ircclient.model.LogDirection;
import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptDocumentSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptLineMetaSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptState;
import cafe.woden.ircclient.ui.chat.transcript.style.ChatTranscriptAttrSupport;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyledDocument;

/** Owns message lookup, reaction updates, and edit/redaction mutation wiring. */
public final class ChatTranscriptMessageInteractionCoordinator {

  private final Map<TargetRef, StyledDocument> docs;
  private final Map<TargetRef, ChatTranscriptState> stateByTarget;
  private final Consumer<TargetRef> ensureTargetExists;
  private final ChatTranscriptMessageCatalogSupport messageCatalogSupport;
  private final ChatTranscriptReactionSummarySupport reactionSummarySupport;
  private final ChatTranscriptMessageMutationSupport messageMutationSupport;
  private final ChatTranscriptMessageTranslationSupport messageTranslationSupport;

  public ChatTranscriptMessageInteractionCoordinator(
      Map<TargetRef, StyledDocument> docs,
      Map<TargetRef, ChatTranscriptState> stateByTarget,
      Consumer<TargetRef> ensureTargetExists,
      BiConsumer<TargetRef, Long> noteEpochMs,
      ChatTranscriptMessageCatalogSupport messageCatalogSupport,
      ChatTranscriptReactionSummarySupport reactionSummarySupport,
      ChatTranscriptSenderStyleSupport.Context senderStyleSupportContext,
      ChatTranscriptMessageReplacementSupport.TranscriptFromRenderer transcriptFromRenderer,
      ChatTranscriptMessageReplacementSupport.ActionLineInserter actionLineInserter,
      ChatTranscriptMessageReplacementSupport.StandardLineInserter standardLineInserter,
      String redactedMessagePlaceholder,
      ChatTranscriptMessageTranslationSupport messageTranslationSupport) {
    this.docs = Objects.requireNonNull(docs, "docs");
    this.stateByTarget = Objects.requireNonNull(stateByTarget, "stateByTarget");
    this.ensureTargetExists = Objects.requireNonNull(ensureTargetExists, "ensureTargetExists");
    Objects.requireNonNull(noteEpochMs, "noteEpochMs");
    this.messageCatalogSupport =
        Objects.requireNonNull(messageCatalogSupport, "messageCatalogSupport");
    this.reactionSummarySupport =
        Objects.requireNonNull(reactionSummarySupport, "reactionSummarySupport");
    this.messageTranslationSupport =
        Objects.requireNonNull(messageTranslationSupport, "messageTranslationSupport");
    Objects.requireNonNull(senderStyleSupportContext, "senderStyleSupportContext");
    Objects.requireNonNull(transcriptFromRenderer, "transcriptFromRenderer");
    Objects.requireNonNull(actionLineInserter, "actionLineInserter");
    Objects.requireNonNull(standardLineInserter, "standardLineInserter");
    Objects.requireNonNull(redactedMessagePlaceholder, "redactedMessagePlaceholder");

    ChatTranscriptMessageReplacementSupport messageReplacementSupport =
        new ChatTranscriptMessageReplacementSupport(
            messageCatalogSupport,
            senderStyleSupportContext,
            (ref, kind, direction, fromNick, epochMs, messageId, ircv3Tags) ->
                ChatTranscriptLineMetaSupport.create(
                    ref, kind, direction, fromNick, epochMs, null, messageId, ircv3Tags),
            transcriptFromRenderer,
            actionLineInserter,
            standardLineInserter,
            noteEpochMs::accept);
    this.messageMutationSupport =
        new ChatTranscriptMessageMutationSupport(
            messageCatalogSupport,
            messageReplacementSupport,
            reactionSummarySupport,
            redactedMessagePlaceholder);
  }

  public int messageOffsetById(TargetRef ref, String messageId) {
    if (ref == null) return -1;
    String msgId = normalizeMessageId(messageId);
    if (msgId.isEmpty()) return -1;
    StyledDocument doc = docs.get(ref);
    if (doc == null) return -1;
    return ChatTranscriptDocumentSupport.findLineStartByMessageId(doc, msgId);
  }

  public String messagePreviewById(TargetRef ref, String messageId) {
    if (ref == null) return "";
    String msgId = normalizeMessageId(messageId);
    if (msgId.isEmpty()) return "";
    ChatTranscriptState state = stateByTarget.get(ref);
    if (state == null) return "";
    return messageCatalogSupport.previewForMessageId(state.messageCatalog(), msgId);
  }

  public RedactedMessageContent redactedOriginalById(TargetRef ref, String messageId) {
    if (ref == null) return null;
    String msgId = normalizeMessageId(messageId);
    if (msgId.isEmpty()) return null;
    ChatTranscriptState state = stateByTarget.get(ref);
    if (state == null) return null;
    return messageCatalogSupport.redactedOriginalById(state.messageCatalog(), msgId);
  }

  public MessageTranslationSource translationSourceById(TargetRef ref, String messageId) {
    if (ref == null) return null;
    String msgId = normalizeMessageId(messageId);
    if (msgId.isEmpty()) return null;
    ChatTranscriptState state = stateByTarget.get(ref);
    if (state == null) return null;
    return messageCatalogSupport.translationSourceById(state.messageCatalog(), msgId);
  }

  public boolean isOwnMessage(TargetRef ref, String messageId) {
    if (ref == null) return false;
    String msgId = normalizeMessageId(messageId);
    if (msgId.isEmpty()) return false;
    StyledDocument doc = docs.get(ref);
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

  public boolean hasReactionFromNick(
      TargetRef ref, String messageId, String reaction, String nick) {
    ChatTranscriptState state = stateByTarget.get(ref);
    return reactionSummarySupport.hasReactionFromNick(
        state == null ? null : state.reactionSummary(), messageId, reaction, nick);
  }

  public void setReactionChipActionHandler(ReactionChipActionHandler handler) {
    Map<TargetRef, ChatTranscriptReactionSummarySupport.State> states = new HashMap<>();
    for (Map.Entry<TargetRef, ChatTranscriptState> entry : stateByTarget.entrySet()) {
      ChatTranscriptState state = entry.getValue();
      if (state != null) {
        states.put(entry.getKey(), state.reactionSummary());
      }
    }
    reactionSummarySupport.setReactionChipActionHandler(handler, states);
  }

  public void applyMessageReaction(
      TargetRef ref, String targetMessageId, String reaction, String fromNick, long tsEpochMs) {
    if (ref == null) return;
    ensureTargetExists.accept(ref);
    StyledDocument doc = docs.get(ref);
    ChatTranscriptState state = stateByTarget.get(ref);
    if (doc == null || state == null) return;
    reactionSummarySupport.applyMessageReaction(
        ref, doc, state.reactionSummary(), targetMessageId, reaction, fromNick, tsEpochMs);
  }

  public void removeMessageReaction(
      TargetRef ref, String targetMessageId, String reaction, String fromNick, long tsEpochMs) {
    if (ref == null) return;
    ensureTargetExists.accept(ref);
    StyledDocument doc = docs.get(ref);
    ChatTranscriptState state = stateByTarget.get(ref);
    if (doc == null || state == null) return;
    reactionSummarySupport.removeMessageReaction(
        ref, doc, state.reactionSummary(), targetMessageId, reaction, fromNick, tsEpochMs);
  }

  public boolean applyMessageTranslation(
      TargetRef ref, MessageTranslation translation, long translatedAtEpochMs) {
    if (ref == null) return false;
    ensureTargetExists.accept(ref);
    StyledDocument doc = docs.get(ref);
    if (doc == null) return false;
    return messageTranslationSupport.applyMessageTranslation(
        ref, doc, translation, translatedAtEpochMs);
  }

  public boolean applyMessageEdit(
      TargetRef ref,
      String targetMessageId,
      String editedText,
      String fromNick,
      long tsEpochMs,
      String replacementMessageId,
      Map<String, String> replacementIrcv3Tags) {
    if (ref == null) return false;
    ensureTargetExists.accept(ref);
    StyledDocument doc = docs.get(ref);
    ChatTranscriptState state = stateByTarget.get(ref);
    return messageMutationSupport.applyMessageEdit(
        ref,
        doc,
        state == null ? null : state.messageCatalog(),
        targetMessageId,
        editedText,
        tsEpochMs,
        replacementMessageId,
        replacementIrcv3Tags);
  }

  public boolean applyMessageRedaction(
      TargetRef ref,
      String targetMessageId,
      String fromNick,
      long tsEpochMs,
      String replacementMessageId,
      Map<String, String> replacementIrcv3Tags) {
    if (ref == null) return false;
    ensureTargetExists.accept(ref);
    StyledDocument doc = docs.get(ref);
    ChatTranscriptState state = stateByTarget.get(ref);
    boolean redacted =
        messageMutationSupport.applyMessageRedaction(
            ref,
            doc,
            state == null ? null : state.messageCatalog(),
            state == null ? null : state.reactionSummary(),
            targetMessageId,
            fromNick,
            tsEpochMs,
            replacementMessageId,
            replacementIrcv3Tags);
    if (redacted) {
      messageTranslationSupport.clearTranslationForMessage(ref, doc, targetMessageId);
    }
    return redacted;
  }
}
