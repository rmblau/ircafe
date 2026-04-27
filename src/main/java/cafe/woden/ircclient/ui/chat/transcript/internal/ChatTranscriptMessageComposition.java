package cafe.woden.ircclient.ui.chat.transcript.internal;

import cafe.woden.ircclient.model.LogDirection;
import cafe.woden.ircclient.model.LogKind;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.NickColorService;
import cafe.woden.ircclient.ui.chat.embed.ChatImageEmbedder;
import cafe.woden.ircclient.ui.chat.embed.ChatLinkPreviewEmbedder;
import cafe.woden.ircclient.ui.chat.render.ChatRichTextRenderer;
import cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStore;
import cafe.woden.ircclient.ui.chat.transcript.filter.ChatTranscriptFilterRoutingSupport;
import cafe.woden.ircclient.ui.chat.transcript.filter.ChatTranscriptFilteredFlowCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptDocumentLineSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptLineMetaSupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMatrixDisplayNameCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageCatalogSupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageInteractionCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageLineCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptReactionSummarySupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptReplyContextSupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptReplyFlowCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptSenderStyleSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTimestampFormatter;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptLineCapSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptRuntimeFlowCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptRuntimeSettingsSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptTargetRuntimeCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.style.ChatTranscriptStyleRoutingSupport;
import java.util.Map;

/** Builds message-oriented collaborators for the transcript store composition. */
final class ChatTranscriptMessageComposition {

  private static final String REDACTED_MESSAGE_PLACEHOLDER = "[message redacted]";

  record Components(
      ChatTranscriptReplyFlowCoordinator replyFlowCoordinator,
      ChatTranscriptMessageLineCoordinator messageLineCoordinator,
      ChatTranscriptMessageInteractionCoordinator messageInteractionCoordinator) {}

  private ChatTranscriptMessageComposition() {}

  static Components create(
      ChatTranscriptStore store,
      ChatStyles styles,
      ChatRichTextRenderer renderer,
      ChatTimestampFormatter ts,
      NickColorService nickColors,
      ChatImageEmbedder imageEmbeds,
      ChatLinkPreviewEmbedder linkPreviews,
      ChatTranscriptMatrixDisplayNameCoordinator matrixDisplayNameCoordinator,
      ChatTranscriptStyleRoutingSupport styleRoutingSupport,
      ChatTranscriptFilterRoutingSupport filterRoutingSupport,
      ChatTranscriptDocumentLineSupport documentLineSupport,
      ChatTranscriptLineCapSupport lineCapSupport,
      ChatTranscriptRuntimeFlowCoordinator runtimeFlowCoordinator,
      ChatTranscriptTargetRuntimeCoordinator targetRuntimeCoordinator,
      ChatTranscriptRuntimeSettingsSupport runtimeSettingsSupport,
      ChatTranscriptMessageCatalogSupport messageCatalogSupport,
      ChatTranscriptFilteredFlowCoordinator filteredFlowCoordinator) {
    ChatTranscriptReplyContextSupport.Context replyContextSupportContext =
        new ChatTranscriptReplyContextSupport.Context(
            styles, ts, matrixDisplayNameCoordinator::renderTranscriptFrom);
    ChatTranscriptReplyFlowCoordinator replyFlowCoordinator =
        new ChatTranscriptReplyFlowCoordinator(
            targetRuntimeCoordinator.docs(),
            targetRuntimeCoordinator.stateByTarget(),
            targetRuntimeCoordinator::ensureTargetExists,
            documentLineSupport,
            replyContextSupportContext,
            messageCatalogSupport);
    ChatTranscriptSenderStyleSupport.Context senderStyleSupportContext =
        new ChatTranscriptSenderStyleSupport.Context(
            styles,
            nickColors,
            ChatTranscriptLineMetaSupport::bind,
            styleRoutingSupport::applyOutgoingLineColor,
            styleRoutingSupport::applyNotificationRuleHighlightColor);
    ChatTranscriptReactionSummarySupport reactionSummarySupport =
        new ChatTranscriptReactionSummarySupport(
            styles,
            styleRoutingSupport::safeTranscriptFont,
            (ref, epochMs, targetMessageId) ->
                ChatTranscriptLineMetaSupport.create(
                    ref,
                    LogKind.STATUS,
                    LogDirection.SYSTEM,
                    null,
                    epochMs,
                    null,
                    targetMessageId,
                    Map.of("draft/react", "1")),
            ChatTranscriptLineMetaSupport::bind,
            ChatTranscriptLineMetaSupport::withAuxiliaryRowKind,
            documentLineSupport::normalizeInsertAtLineStart,
            documentLineSupport::ensureAtLineStartForInsert,
            runtimeFlowCoordinator::shiftCurrentBlock);
    ChatTranscriptMessageLineCoordinator messageLineCoordinator =
        new ChatTranscriptMessageLineCoordinator(
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
            replyFlowCoordinator::appendReplyContextLine,
            matrixDisplayNameCoordinator::renderTranscriptFrom,
            filteredFlowCoordinator::endInsertRun,
            filteredFlowCoordinator::shouldDeferRichTextDuringHistoryBatch);
    ChatTranscriptMessageInteractionCoordinator messageInteractionCoordinator =
        new ChatTranscriptMessageInteractionCoordinator(
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
    return new Components(
        replyFlowCoordinator, messageLineCoordinator, messageInteractionCoordinator);
  }
}
