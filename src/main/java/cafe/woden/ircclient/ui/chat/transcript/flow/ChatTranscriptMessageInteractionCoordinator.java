package cafe.woden.ircclient.ui.chat.transcript.flow;

import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStore;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptLineMetaSupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageCatalogSupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageMutationSupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageQuerySupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageReplacementSupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptReactionSummarySupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptSenderStyleSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptState;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.swing.text.StyledDocument;

/** Owns message lookup, reaction updates, and edit/redaction mutation wiring. */
public final class ChatTranscriptMessageInteractionCoordinator {

  private final ChatTranscriptMessageQuerySupport messageQuerySupport =
      new ChatTranscriptMessageQuerySupport();
  private final ChatTranscriptReactionFlowSupport reactionFlowSupport =
      new ChatTranscriptReactionFlowSupport();
  private final ChatTranscriptMessageMutationFlowSupport messageMutationFlowSupport =
      new ChatTranscriptMessageMutationFlowSupport();
  private final ChatTranscriptMessageQuerySupport.Context messageQueryContext;
  private final ChatTranscriptReactionFlowSupport.Context reactionFlowContext;
  private final ChatTranscriptMessageMutationFlowSupport.Context messageMutationFlowContext;

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
      String redactedMessagePlaceholder) {
    Objects.requireNonNull(docs, "docs");
    Objects.requireNonNull(stateByTarget, "stateByTarget");
    Objects.requireNonNull(ensureTargetExists, "ensureTargetExists");
    Objects.requireNonNull(noteEpochMs, "noteEpochMs");
    Objects.requireNonNull(messageCatalogSupport, "messageCatalogSupport");
    Objects.requireNonNull(reactionSummarySupport, "reactionSummarySupport");
    Objects.requireNonNull(senderStyleSupportContext, "senderStyleSupportContext");
    Objects.requireNonNull(transcriptFromRenderer, "transcriptFromRenderer");
    Objects.requireNonNull(actionLineInserter, "actionLineInserter");
    Objects.requireNonNull(standardLineInserter, "standardLineInserter");
    Objects.requireNonNull(redactedMessagePlaceholder, "redactedMessagePlaceholder");

    this.messageQueryContext =
        new ChatTranscriptMessageQuerySupport.Context(docs, stateByTarget, messageCatalogSupport);
    this.reactionFlowContext =
        new ChatTranscriptReactionFlowSupport.Context(
            docs, stateByTarget, ensureTargetExists, reactionSummarySupport);

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
    ChatTranscriptMessageMutationSupport messageMutationSupport =
        new ChatTranscriptMessageMutationSupport(
            messageCatalogSupport,
            messageReplacementSupport,
            reactionSummarySupport,
            redactedMessagePlaceholder);
    this.messageMutationFlowContext =
        new ChatTranscriptMessageMutationFlowSupport.Context(
            docs, stateByTarget, ensureTargetExists, messageMutationSupport);
  }

  public int messageOffsetById(TargetRef ref, String messageId) {
    return messageQuerySupport.messageOffsetById(messageQueryContext, ref, messageId);
  }

  public String messagePreviewById(TargetRef ref, String messageId) {
    return messageQuerySupport.messagePreviewById(messageQueryContext, ref, messageId);
  }

  public ChatTranscriptStore.RedactedMessageContent redactedOriginalById(
      TargetRef ref, String messageId) {
    return messageQuerySupport.redactedOriginalById(messageQueryContext, ref, messageId);
  }

  public boolean isOwnMessage(TargetRef ref, String messageId) {
    return messageQuerySupport.isOwnMessage(messageQueryContext, ref, messageId);
  }

  public boolean hasReactionFromNick(
      TargetRef ref, String messageId, String reaction, String nick) {
    return reactionFlowSupport.hasReactionFromNick(
        reactionFlowContext, ref, messageId, reaction, nick);
  }

  public void setReactionChipActionHandler(ChatTranscriptStore.ReactionChipActionHandler handler) {
    reactionFlowSupport.setReactionChipActionHandler(reactionFlowContext, handler);
  }

  public void applyMessageReaction(
      TargetRef ref, String targetMessageId, String reaction, String fromNick, long tsEpochMs) {
    reactionFlowSupport.applyMessageReaction(
        reactionFlowContext, ref, targetMessageId, reaction, fromNick, tsEpochMs);
  }

  public void removeMessageReaction(
      TargetRef ref, String targetMessageId, String reaction, String fromNick, long tsEpochMs) {
    reactionFlowSupport.removeMessageReaction(
        reactionFlowContext, ref, targetMessageId, reaction, fromNick, tsEpochMs);
  }

  public boolean applyMessageEdit(
      TargetRef ref,
      String targetMessageId,
      String editedText,
      String fromNick,
      long tsEpochMs,
      String replacementMessageId,
      Map<String, String> replacementIrcv3Tags) {
    return messageMutationFlowSupport.applyMessageEdit(
        messageMutationFlowContext,
        ref,
        targetMessageId,
        editedText,
        fromNick,
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
    return messageMutationFlowSupport.applyMessageRedaction(
        messageMutationFlowContext,
        ref,
        targetMessageId,
        fromNick,
        tsEpochMs,
        replacementMessageId,
        replacementIrcv3Tags);
  }
}
