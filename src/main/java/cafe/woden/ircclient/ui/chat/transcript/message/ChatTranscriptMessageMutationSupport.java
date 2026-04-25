package cafe.woden.ircclient.ui.chat.transcript.message;

import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptDocumentSupport;
import java.util.Map;
import java.util.Objects;
import javax.swing.text.StyledDocument;

public final class ChatTranscriptMessageMutationSupport {
  private final ChatTranscriptMessageCatalogSupport messageCatalogSupport;
  private final ChatTranscriptMessageReplacementSupport messageReplacementSupport;
  private final ChatTranscriptReactionSummarySupport reactionSummarySupport;
  private final String redactedMessagePlaceholder;

  public ChatTranscriptMessageMutationSupport(
      ChatTranscriptMessageCatalogSupport messageCatalogSupport,
      ChatTranscriptMessageReplacementSupport messageReplacementSupport,
      ChatTranscriptReactionSummarySupport reactionSummarySupport,
      String redactedMessagePlaceholder) {
    this.messageCatalogSupport =
        Objects.requireNonNull(messageCatalogSupport, "messageCatalogSupport");
    this.messageReplacementSupport =
        Objects.requireNonNull(messageReplacementSupport, "messageReplacementSupport");
    this.reactionSummarySupport =
        Objects.requireNonNull(reactionSummarySupport, "reactionSummarySupport");
    this.redactedMessagePlaceholder =
        Objects.requireNonNull(redactedMessagePlaceholder, "redactedMessagePlaceholder");
  }

  public boolean applyMessageEdit(
      TargetRef ref,
      StyledDocument doc,
      ChatTranscriptMessageCatalogSupport.State messageCatalog,
      String targetMessageId,
      String editedText,
      long tsEpochMs,
      String replacementMessageId,
      Map<String, String> replacementIrcv3Tags) {
    ChatTranscriptDocumentSupport.MessageLine line =
        ChatTranscriptDocumentSupport.findMessageLine(doc, targetMessageId);
    if (line == null) return false;

    String renderedEditedText = ChatTranscriptMessageEditSupport.renderEditedText(editedText);
    boolean replaced =
        messageReplacementSupport.replaceMessageLine(
            ref,
            doc,
            line.lineStart(),
            line.attrs(),
            renderedEditedText,
            tsEpochMs,
            replacementMessageId,
            replacementIrcv3Tags,
            messageCatalog);
    if (replaced) {
      messageCatalogSupport.rememberEditedCurrentMessageContent(
          messageCatalog, line.targetMessageId(), line.attrs(), renderedEditedText);
    }
    return replaced;
  }

  public boolean applyMessageRedaction(
      TargetRef ref,
      StyledDocument doc,
      ChatTranscriptMessageCatalogSupport.State messageCatalog,
      ChatTranscriptReactionSummarySupport.State reactionSummary,
      String targetMessageId,
      String fromNick,
      long tsEpochMs,
      String replacementMessageId,
      Map<String, String> replacementIrcv3Tags) {
    ChatTranscriptDocumentSupport.MessageLine line =
        ChatTranscriptDocumentSupport.findMessageLine(doc, targetMessageId);
    if (line == null) return false;

    messageCatalogSupport.rememberRedactedOriginal(
        messageCatalog, line.targetMessageId(), line.attrs(), fromNick, tsEpochMs);

    boolean replaced =
        messageReplacementSupport.replaceMessageLine(
            ref,
            doc,
            line.lineStart(),
            line.attrs(),
            redactedMessagePlaceholder,
            tsEpochMs,
            replacementMessageId,
            replacementIrcv3Tags,
            messageCatalog);
    if (!replaced) return false;

    ChatTranscriptDocumentSupport.markLineRangeRedacted(doc, line.lineStart());
    messageCatalogSupport.clearCurrentMessageContent(messageCatalog, line.targetMessageId());
    reactionSummarySupport.clearReactionStateForMessage(
        ref, doc, reactionSummary, line.targetMessageId());
    return true;
  }
}
