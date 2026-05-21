package cafe.woden.ircclient.ui.chat.transcript.message;

import static cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageMetadataSupport.firstIrcv3TagValue;
import static cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageMetadataSupport.normalizeMessageId;
import static cafe.woden.ircclient.util.Ircv3CapabilityNames.DRAFT_REACT;
import static cafe.woden.ircclient.util.Ircv3CapabilityNames.DRAFT_REPLY;
import static cafe.woden.ircclient.util.Ircv3CapabilityNames.REPLY;

import cafe.woden.ircclient.model.TargetRef;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import javax.swing.text.StyledDocument;

/** Shared helpers for outgoing transcript follow-up triggered by IRCv3 message tags. */
public final class ChatTranscriptOutgoingFollowUpSupport {

  public record Plan(String normalizedMessageId, String replyToMessageId, String reactionToken) {
    public boolean hasReplyContext() {
      return !replyToMessageId.isBlank();
    }

    public boolean hasMaterializedMessageId() {
      return !normalizedMessageId.isBlank();
    }

    public boolean hasReplyReaction() {
      return !replyToMessageId.isBlank() && !reactionToken.isBlank();
    }

    public void runReplyContext(Consumer<String> callback) {
      if (callback != null && hasReplyContext()) {
        callback.accept(replyToMessageId);
      }
    }

    public void runPendingMaterialization(Runnable callback) {
      if (callback != null && hasMaterializedMessageId()) {
        callback.run();
      }
    }

    public void runReplyReaction(Runnable callback) {
      if (callback != null && hasReplyReaction()) {
        callback.run();
      }
    }

    public void applyPostAppend(
        TargetRef ref,
        StyledDocument doc,
        ChatTranscriptReactionSummarySupport reactionSummarySupport,
        ChatTranscriptReactionSummarySupport.State reactionSummaryState,
        String fromNick,
        long tsEpochMs) {
      if (ref == null
          || doc == null
          || reactionSummarySupport == null
          || reactionSummaryState == null) {
        return;
      }

      runPendingMaterialization(
          () ->
              reactionSummarySupport.materializePendingReactionsForMessage(
                  ref, doc, reactionSummaryState, normalizedMessageId, tsEpochMs));
      runReplyReaction(
          () ->
              reactionSummarySupport.applyMessageReaction(
                  ref,
                  doc,
                  reactionSummaryState,
                  replyToMessageId,
                  reactionToken,
                  fromNick,
                  tsEpochMs));
    }
  }

  private ChatTranscriptOutgoingFollowUpSupport() {}

  public static Plan plan(String messageId, Map<String, String> ircv3Tags) {
    String normalizedMessageId = normalizeMessageId(messageId);
    String replyToMessageId =
        firstIrcv3TagValue(ircv3Tags, REPLY, "+" + REPLY, DRAFT_REPLY, "+" + DRAFT_REPLY);
    String reactionToken = firstIrcv3TagValue(ircv3Tags, DRAFT_REACT, "+" + DRAFT_REACT);
    return new Plan(
        normalizedMessageId,
        Objects.toString(replyToMessageId, "").trim(),
        Objects.toString(reactionToken, "").trim());
  }
}
