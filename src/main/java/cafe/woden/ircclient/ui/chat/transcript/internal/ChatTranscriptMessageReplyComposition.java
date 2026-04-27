package cafe.woden.ircclient.ui.chat.transcript.internal;

import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptDocumentLineSupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageCatalogSupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptReplyContextSupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptReplyFlowCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptTargetRuntimeCoordinator;

/** Builds reply-flow orchestration for message-oriented transcript composition. */
final class ChatTranscriptMessageReplyComposition {

  private ChatTranscriptMessageReplyComposition() {}

  static ChatTranscriptReplyFlowCoordinator create(
      ChatTranscriptTargetRuntimeCoordinator targetRuntimeCoordinator,
      ChatTranscriptDocumentLineSupport documentLineSupport,
      ChatTranscriptReplyContextSupport.Context replyContextSupportContext,
      ChatTranscriptMessageCatalogSupport messageCatalogSupport) {
    return new ChatTranscriptReplyFlowCoordinator(
        targetRuntimeCoordinator.docs(),
        targetRuntimeCoordinator.stateByTarget(),
        targetRuntimeCoordinator::ensureTargetExists,
        documentLineSupport,
        replyContextSupportContext,
        messageCatalogSupport);
  }
}
