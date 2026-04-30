package cafe.woden.ircclient.ui.chat.transcript.internal;

import cafe.woden.ircclient.irc.roster.UserListPort;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.NickColorService;
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
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageInteractionCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageLineCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptReplyFlowSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTimestampFormatter;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptLineCapSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptRuntimeFlowCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptRuntimeSettingsSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptTargetRuntimeCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.style.ChatTranscriptStyleRoutingSupport;
import cafe.woden.ircclient.ui.settings.UiSettingsBus;

/** Builds message-oriented collaborators for the transcript store composition. */
final class ChatTranscriptMessageComposition {

  record Components(
      ChatTranscriptMatrixDisplayNameCoordinator matrixDisplayNameCoordinator,
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
      UiSettingsBus uiSettings,
      UserListPort userListStore,
      ChatTranscriptStyleRoutingSupport styleRoutingSupport,
      ChatTranscriptFilterRoutingSupport filterRoutingSupport,
      ChatTranscriptDocumentLineSupport documentLineSupport,
      ChatTranscriptLineCapSupport lineCapSupport,
      ChatTranscriptRuntimeFlowCoordinator runtimeFlowCoordinator,
      ChatTranscriptTargetRuntimeCoordinator targetRuntimeCoordinator,
      ChatTranscriptRuntimeSettingsSupport runtimeSettingsSupport,
      ChatTranscriptMessageCatalogSupport messageCatalogSupport,
      ChatTranscriptFilteredFlowCoordinator filteredFlowCoordinator) {
    ChatTranscriptMatrixDisplayNameCoordinator matrixDisplayNameCoordinator =
        new ChatTranscriptMatrixDisplayNameCoordinator(
            uiSettings, userListStore, targetRuntimeCoordinator.docs());
    ChatTranscriptMessageSupportComposition.Components messageSupportComposition =
        ChatTranscriptMessageSupportComposition.create(
            styles,
            ts,
            nickColors,
            matrixDisplayNameCoordinator,
            styleRoutingSupport,
            documentLineSupport,
            runtimeFlowCoordinator);
    ChatTranscriptReplyFlowSupport replyFlowSupport = new ChatTranscriptReplyFlowSupport();
    ChatTranscriptReplyFlowSupport.Context replyFlowContext =
        new ChatTranscriptReplyFlowSupport.Context(
            targetRuntimeCoordinator.docs(),
            targetRuntimeCoordinator.stateByTarget(),
            targetRuntimeCoordinator::ensureTargetExists,
            documentLineSupport,
            messageSupportComposition.replyContextSupportContext(),
            messageCatalogSupport);
    ChatTranscriptActionFlowSupport.ReplyContextAppender appendReplyContextLine =
        (ref, fromNick, replyToMsgId, tsEpochMs) ->
            replyFlowSupport.appendReplyContextLine(
                replyFlowContext, ref, fromNick, replyToMsgId, tsEpochMs);
    ChatTranscriptMessageLineCoordinator messageLineCoordinator =
        ChatTranscriptMessageLineComposition.create(
            store,
            styles,
            renderer,
            ts,
            imageEmbeds,
            linkPreviews,
            styleRoutingSupport,
            runtimeSettingsSupport,
            filterRoutingSupport,
            documentLineSupport,
            lineCapSupport,
            runtimeFlowCoordinator,
            messageSupportComposition.senderStyleSupportContext(),
            messageCatalogSupport,
            messageSupportComposition.reactionSummarySupport(),
            targetRuntimeCoordinator,
            appendReplyContextLine,
            matrixDisplayNameCoordinator,
            filteredFlowCoordinator);
    ChatTranscriptMessageInteractionCoordinator messageInteractionCoordinator =
        ChatTranscriptMessageInteractionComposition.create(
            targetRuntimeCoordinator,
            messageCatalogSupport,
            messageSupportComposition.reactionSummarySupport(),
            messageSupportComposition.senderStyleSupportContext(),
            matrixDisplayNameCoordinator,
            messageLineCoordinator,
            runtimeFlowCoordinator);
    return new Components(
        matrixDisplayNameCoordinator, messageLineCoordinator, messageInteractionCoordinator);
  }
}
