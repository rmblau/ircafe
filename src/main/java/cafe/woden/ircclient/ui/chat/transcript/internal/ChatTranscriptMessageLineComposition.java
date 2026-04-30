package cafe.woden.ircclient.ui.chat.transcript.internal;

import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.embed.ChatImageEmbedder;
import cafe.woden.ircclient.ui.chat.embed.ChatLinkPreviewEmbedder;
import cafe.woden.ircclient.ui.chat.render.ChatRichTextRenderer;
import cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStore;
import cafe.woden.ircclient.ui.chat.transcript.filter.ChatTranscriptFilterRoutingSupport;
import cafe.woden.ircclient.ui.chat.transcript.filter.ChatTranscriptFilteredFlowCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptDocumentLineSupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptActionFlowSupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMatrixDisplayNameCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageCatalogSupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageLineCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptReactionSummarySupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptSenderStyleSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTimestampFormatter;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptLineCapSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptRuntimeFlowCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptRuntimeSettingsSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptTargetRuntimeCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.style.ChatTranscriptStyleRoutingSupport;

/** Builds message line orchestration for message-oriented transcript composition. */
final class ChatTranscriptMessageLineComposition {

  private ChatTranscriptMessageLineComposition() {}

  static ChatTranscriptMessageLineCoordinator create(
      ChatTranscriptStore store,
      ChatStyles styles,
      ChatRichTextRenderer renderer,
      ChatTimestampFormatter ts,
      ChatImageEmbedder imageEmbeds,
      ChatLinkPreviewEmbedder linkPreviews,
      ChatTranscriptStyleRoutingSupport styleRoutingSupport,
      ChatTranscriptRuntimeSettingsSupport runtimeSettingsSupport,
      ChatTranscriptFilterRoutingSupport filterRoutingSupport,
      ChatTranscriptDocumentLineSupport documentLineSupport,
      ChatTranscriptLineCapSupport lineCapSupport,
      ChatTranscriptRuntimeFlowCoordinator runtimeFlowCoordinator,
      ChatTranscriptSenderStyleSupport.Context senderStyleSupportContext,
      ChatTranscriptMessageCatalogSupport messageCatalogSupport,
      ChatTranscriptReactionSummarySupport reactionSummarySupport,
      ChatTranscriptTargetRuntimeCoordinator targetRuntimeCoordinator,
      ChatTranscriptActionFlowSupport.ReplyContextAppender appendReplyContextLine,
      ChatTranscriptMatrixDisplayNameCoordinator matrixDisplayNameCoordinator,
      ChatTranscriptFilteredFlowCoordinator filteredFlowCoordinator) {
    return new ChatTranscriptMessageLineCoordinator(
        store,
        styles,
        ts,
        renderer,
        imageEmbeds,
        linkPreviews,
        styleRoutingSupport,
        runtimeSettingsSupport,
        filterRoutingSupport,
        documentLineSupport,
        lineCapSupport,
        runtimeFlowCoordinator,
        senderStyleSupportContext,
        messageCatalogSupport,
        reactionSummarySupport,
        targetRuntimeCoordinator.docs(),
        targetRuntimeCoordinator.stateByTarget(),
        targetRuntimeCoordinator::ensureTargetExists,
        targetRuntimeCoordinator::noteEpochMs,
        appendReplyContextLine,
        matrixDisplayNameCoordinator::renderTranscriptFrom,
        filteredFlowCoordinator::endInsertRun,
        filteredFlowCoordinator::shouldDeferRichTextDuringHistoryBatch);
  }
}
