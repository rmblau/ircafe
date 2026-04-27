package cafe.woden.ircclient.ui.chat.transcript.internal;

import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMatrixDisplayNameCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageCatalogSupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageInteractionCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageLineCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptReactionSummarySupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptSenderStyleSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptRuntimeFlowCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptTargetRuntimeCoordinator;

/** Builds message interaction collaborators for message-oriented transcript composition. */
final class ChatTranscriptMessageInteractionComposition {

  private static final String REDACTED_MESSAGE_PLACEHOLDER = "[message redacted]";

  private ChatTranscriptMessageInteractionComposition() {}

  static ChatTranscriptMessageInteractionCoordinator create(
      ChatTranscriptTargetRuntimeCoordinator targetRuntimeCoordinator,
      ChatTranscriptMessageCatalogSupport messageCatalogSupport,
      ChatTranscriptReactionSummarySupport reactionSummarySupport,
      ChatTranscriptSenderStyleSupport.Context senderStyleSupportContext,
      ChatTranscriptMatrixDisplayNameCoordinator matrixDisplayNameCoordinator,
      ChatTranscriptMessageLineCoordinator messageLineCoordinator,
      ChatTranscriptRuntimeFlowCoordinator runtimeFlowCoordinator) {
    return new ChatTranscriptMessageInteractionCoordinator(
        targetRuntimeCoordinator.docs(),
        targetRuntimeCoordinator.stateByTarget(),
        targetRuntimeCoordinator::ensureTargetExists,
        targetRuntimeCoordinator::noteEpochMs,
        messageCatalogSupport,
        reactionSummarySupport,
        senderStyleSupportContext,
        matrixDisplayNameCoordinator::renderTranscriptFrom,
        messageLineCoordinator::insertReplacementAction,
        (ref, insertAt, from, text, fromStyle, messageStyle, meta) ->
            runtimeFlowCoordinator.insertLineAt(
                ref, insertAt, from, text, fromStyle, messageStyle, meta),
        REDACTED_MESSAGE_PLACEHOLDER);
  }
}
