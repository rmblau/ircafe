package cafe.woden.ircclient.ui.chat.transcript.internal;

import cafe.woden.ircclient.model.LogDirection;
import cafe.woden.ircclient.model.LogKind;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.NickColorService;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptDocumentLineSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptLineMetaSupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMatrixDisplayNameCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptReactionSummarySupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptReplyContextSupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptSenderStyleSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTimestampFormatter;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptRuntimeFlowCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.style.ChatTranscriptStyleRoutingSupport;
import java.util.Map;

/** Builds reusable support contexts for message-oriented transcript composition. */
final class ChatTranscriptMessageSupportComposition {

  record Components(
      ChatTranscriptReplyContextSupport.Context replyContextSupportContext,
      ChatTranscriptSenderStyleSupport.Context senderStyleSupportContext,
      ChatTranscriptReactionSummarySupport reactionSummarySupport) {}

  private ChatTranscriptMessageSupportComposition() {}

  static Components create(
      ChatStyles styles,
      ChatTimestampFormatter ts,
      NickColorService nickColors,
      ChatTranscriptMatrixDisplayNameCoordinator matrixDisplayNameCoordinator,
      ChatTranscriptStyleRoutingSupport styleRoutingSupport,
      ChatTranscriptDocumentLineSupport documentLineSupport,
      ChatTranscriptRuntimeFlowCoordinator runtimeFlowCoordinator) {
    ChatTranscriptReplyContextSupport.Context replyContextSupportContext =
        new ChatTranscriptReplyContextSupport.Context(
            styles, ts, matrixDisplayNameCoordinator::renderTranscriptFrom);
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
    return new Components(
        replyContextSupportContext, senderStyleSupportContext, reactionSummarySupport);
  }
}
