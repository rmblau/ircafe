package cafe.woden.ircclient.ui.chat.transcript.internal;

import cafe.woden.ircclient.irc.roster.UserListPort;
import cafe.woden.ircclient.model.LogDirection;
import cafe.woden.ircclient.model.LogKind;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.NickColorService;
import cafe.woden.ircclient.ui.chat.NickColorSettingsBus;
import cafe.woden.ircclient.ui.chat.embed.ChatImageEmbedder;
import cafe.woden.ircclient.ui.chat.embed.ChatLinkPreviewEmbedder;
import cafe.woden.ircclient.ui.chat.render.ChatRichTextRenderer;
import cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStore;
import cafe.woden.ircclient.ui.chat.transcript.filter.ChatTranscriptFilterRoutingSupport;
import cafe.woden.ircclient.ui.chat.transcript.filter.ChatTranscriptFilteredLinesSupport;
import cafe.woden.ircclient.ui.chat.transcript.filter.ChatTranscriptFilteredRunSupport;
import cafe.woden.ircclient.ui.chat.transcript.filter.ChatTranscriptFilteredFlowCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageInteractionCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageLineCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.spoiler.ChatTranscriptPlainSpoilerCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptReplyFlowCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptRuntimeFlowCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.spoiler.ChatTranscriptSpoilerFlowSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptAuxiliaryRowsSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptDocumentLineSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptLineMetaSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptPlainAppendSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptPresenceFoldSupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMatrixDisplayNameCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageCatalogSupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageStateSupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptReactionSummarySupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptReplyContextSupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptSenderStyleSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTimestampFormatter;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptLineCapSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptRestyleSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptRuntimeSettingsSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptTargetRuntimeCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.spoiler.ChatTranscriptSpoilerAppendSupport;
import cafe.woden.ircclient.ui.chat.transcript.spoiler.ChatTranscriptSpoilerComponentSupport;
import cafe.woden.ircclient.ui.chat.transcript.spoiler.ChatTranscriptSpoilerHistoryInsertSupport;
import cafe.woden.ircclient.ui.chat.transcript.spoiler.ChatTranscriptSpoilerRevealSupport;
import cafe.woden.ircclient.ui.chat.transcript.spoiler.ChatTranscriptSpoilerRuntimeSupport;
import cafe.woden.ircclient.ui.chat.transcript.spoiler.ChatTranscriptSpoilerWriteSupport;
import cafe.woden.ircclient.ui.chat.transcript.style.ChatTranscriptStyleRoutingSupport;
import cafe.woden.ircclient.ui.filter.FilterEngine;
import cafe.woden.ircclient.ui.settings.UiSettingsBus;
import java.util.Map;

/** Wires the transcript helper graph used by {@link ChatTranscriptStore}. */
public final class ChatTranscriptStoreComposition {

  private static final int REPLY_PREVIEW_CACHE_LIMIT_PER_TARGET = 512;
  private static final int REDACTED_MESSAGE_CACHE_LIMIT_PER_TARGET = 512;
  private static final int REPLY_PREVIEW_TEXT_MAX_CHARS = 120;
  private static final String REDACTED_MESSAGE_PLACEHOLDER = "[message redacted]";

  public record Components(
      ChatTranscriptFilteredFlowCoordinator filteredFlowCoordinator,
      ChatTranscriptMatrixDisplayNameCoordinator matrixDisplayNameCoordinator,
      ChatTranscriptReplyFlowCoordinator replyFlowCoordinator,
      ChatTranscriptMessageInteractionCoordinator messageInteractionCoordinator,
      ChatTranscriptMessageLineCoordinator messageLineCoordinator,
      ChatTranscriptPlainSpoilerCoordinator plainSpoilerCoordinator,
      ChatTranscriptRuntimeFlowCoordinator runtimeFlowCoordinator,
      ChatTranscriptTargetRuntimeCoordinator targetRuntimeCoordinator) {}

  private ChatTranscriptStoreComposition() {}

  public static Components create(
      ChatTranscriptStore store,
      ChatStyles styles,
      ChatRichTextRenderer renderer,
      ChatTimestampFormatter ts,
      NickColorService nickColors,
      NickColorSettingsBus nickColorSettings,
      ChatImageEmbedder imageEmbeds,
      ChatLinkPreviewEmbedder linkPreviews,
      UiSettingsBus uiSettings,
      FilterEngine filterEngine,
      UserListPort userListStore) {

    ChatTranscriptRuntimeFlowCoordinator runtimeFlowCoordinator =
        new ChatTranscriptRuntimeFlowCoordinator(store, styles);

    ChatTranscriptRuntimeSettingsSupport runtimeSettingsSupport =
        new ChatTranscriptRuntimeSettingsSupport(uiSettings, styles);
    ChatTranscriptStyleRoutingSupport styleRoutingSupport =
        new ChatTranscriptStyleRoutingSupport(
            styles,
            runtimeSettingsSupport::safeSettings,
            runtimeSettingsSupport::configuredOutgoingLineColor);
    ChatTranscriptLineCapSupport lineCapSupport =
        new ChatTranscriptLineCapSupport(
            runtimeSettingsSupport::transcriptMaxLinesPerTarget,
            runtimeFlowCoordinator::resetAfterHeadTrim,
            ref -> runtimeFlowCoordinator.maybeRenderPendingReadMarker(ref, null));
    ChatTranscriptRestyleSupport.Context restyleSupportContext =
        new ChatTranscriptRestyleSupport.Context(
            styles, nickColors, styleRoutingSupport::applyFilterActionStyle);
    ChatTranscriptMessageStateSupport.Context messageStateSupportContext =
        new ChatTranscriptMessageStateSupport.Context(
            REPLY_PREVIEW_TEXT_MAX_CHARS, REDACTED_MESSAGE_PLACEHOLDER, System::currentTimeMillis);
    ChatTranscriptMessageCatalogSupport messageCatalogSupport =
        new ChatTranscriptMessageCatalogSupport(messageStateSupportContext);
    ChatTranscriptTargetRuntimeCoordinator targetRuntimeCoordinator =
        new ChatTranscriptTargetRuntimeCoordinator(
            () ->
                messageCatalogSupport.createState(
                    REPLY_PREVIEW_CACHE_LIMIT_PER_TARGET, REDACTED_MESSAGE_CACHE_LIMIT_PER_TARGET),
            store,
            180,
            restyleSupportContext,
            runtimeSettingsSupport::safeSettings,
            runtimeSettingsSupport::configuredOutgoingLineColor,
            nickColorSettings);

    ChatTranscriptFilteredRunSupport.Context filteredRunSupportContext =
        new ChatTranscriptFilteredRunSupport.Context(styles, ChatTranscriptLineMetaSupport::bind);
    ChatTranscriptDocumentLineSupport documentLineSupport =
        new ChatTranscriptDocumentLineSupport(styles);
    ChatTranscriptFilteredLinesSupport filteredLinesSupport =
        new ChatTranscriptFilteredLinesSupport(
            styles,
            filteredRunSupportContext,
            styleRoutingSupport::safeTranscriptFont,
            ChatTranscriptLineMetaSupport::bind,
            documentLineSupport::ensureAtLineStart,
            documentLineSupport::normalizeInsertAtLineStart,
            documentLineSupport::ensureAtLineStartForInsert,
            runtimeFlowCoordinator::breakPresenceRun,
            runtimeFlowCoordinator::shiftCurrentBlock,
            lineCapSupport::enforceTranscriptLineCap);
    ChatTranscriptFilteredFlowCoordinator filteredFlowCoordinator =
        new ChatTranscriptFilteredFlowCoordinator(filteredLinesSupport);
    ChatTranscriptFilterRoutingSupport filterRoutingSupport =
        new ChatTranscriptFilterRoutingSupport(
            filterEngine,
            filteredFlowCoordinator::onFilteredLineAppend,
            filteredFlowCoordinator::onFilteredLineInsertAt,
            filteredFlowCoordinator::endInsertRun,
            runtimeFlowCoordinator::breakPresenceRun);
    filteredFlowCoordinator.bindContext(
        filterRoutingSupport,
        targetRuntimeCoordinator.docs(),
        targetRuntimeCoordinator.stateByTarget(),
        targetRuntimeCoordinator::ensureTargetExists,
        targetRuntimeCoordinator::noteEpochMs,
        runtimeSettingsSupport::chatHistoryDeferRichTextDuringBatch);
    ChatTranscriptPresenceFoldSupport presenceFoldSupport =
        new ChatTranscriptPresenceFoldSupport(
            styles,
            renderer,
            ts,
            ChatTranscriptLineMetaSupport::bind,
            ChatTranscriptLineMetaSupport::withExistingMeta,
            styleRoutingSupport::withFilterMatch,
            documentLineSupport::ensureAtLineStart,
            lineCapSupport::enforceTranscriptLineCap);
    ChatTranscriptMatrixDisplayNameCoordinator matrixDisplayNameCoordinator =
        new ChatTranscriptMatrixDisplayNameCoordinator(
            uiSettings, userListStore, targetRuntimeCoordinator.docs());
    ChatTranscriptSpoilerComponentSupport.Context spoilerComponentSupportContext =
        new ChatTranscriptSpoilerComponentSupport.Context(
            uiSettings, nickColors, matrixDisplayNameCoordinator::renderTranscriptFrom);
    ChatTranscriptSpoilerWriteSupport.Context spoilerWriteSupportContext =
        new ChatTranscriptSpoilerWriteSupport.Context(
            styles, spoilerComponentSupportContext, styleRoutingSupport::withFilterMatch);
    ChatTranscriptSpoilerRevealSupport.Context spoilerRevealSupportContext =
        new ChatTranscriptSpoilerRevealSupport.Context(
            styles, renderer, nickColors, matrixDisplayNameCoordinator::renderTranscriptFrom);
    ChatTranscriptSpoilerRuntimeSupport.Context spoilerRuntimeSupportContext =
        new ChatTranscriptSpoilerRuntimeSupport.Context(
            ts,
            runtimeSettingsSupport::timestampsIncludeChatMessages,
            spoilerRevealSupportContext,
            store);
    ChatTranscriptSpoilerAppendSupport.Context spoilerAppendSupportContext =
        new ChatTranscriptSpoilerAppendSupport.Context(
            styles,
            spoilerWriteSupportContext,
            documentLineSupport::ensureAtLineStart,
            lineCapSupport::enforceTranscriptLineCap);
    ChatTranscriptSpoilerHistoryInsertSupport.Context spoilerHistoryInsertSupportContext =
        new ChatTranscriptSpoilerHistoryInsertSupport.Context(
            spoilerWriteSupportContext,
            documentLineSupport::normalizeInsertAtLineStart,
            documentLineSupport::ensureAtLineStartForInsert,
            runtimeFlowCoordinator::shiftCurrentBlock,
            lineCapSupport::enforceTranscriptLineCap);
    ChatTranscriptSpoilerFlowSupport.Context spoilerFlowSupportContext =
        new ChatTranscriptSpoilerFlowSupport.Context(
            targetRuntimeCoordinator.docs(),
            targetRuntimeCoordinator::ensureTargetExists,
            targetRuntimeCoordinator::noteEpochMs,
            filterRoutingSupport,
            spoilerRuntimeSupportContext,
            spoilerAppendSupportContext,
            spoilerHistoryInsertSupportContext,
            filteredFlowCoordinator::endInsertRun);
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
    ChatTranscriptAuxiliaryRowsSupport auxiliaryRowsSupport =
        new ChatTranscriptAuxiliaryRowsSupport(
            styles,
            styleRoutingSupport::safeTranscriptFont,
            (ref, epochMs) ->
                ChatTranscriptLineMetaSupport.create(
                    ref, LogKind.STATUS, LogDirection.SYSTEM, null, epochMs, null),
            ChatTranscriptLineMetaSupport::bind,
            ChatTranscriptLineMetaSupport::withAuxiliaryRowKind,
            ChatTranscriptLineMetaSupport::withExistingMeta,
            documentLineSupport::normalizeInsertAtLineStart,
            documentLineSupport::ensureAtLineStartForInsert,
            runtimeFlowCoordinator::shiftCurrentBlock);
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
    runtimeFlowCoordinator.bindPresenceContext(
        presenceFoldSupport,
        filterRoutingSupport,
        filteredFlowCoordinator.filteredLinesSupport(),
        runtimeSettingsSupport,
        targetRuntimeCoordinator.docs(),
        targetRuntimeCoordinator.stateByTarget(),
        targetRuntimeCoordinator::ensureTargetExists,
        targetRuntimeCoordinator::noteEpochMs,
        System::currentTimeMillis);
    ChatTranscriptPlainAppendSupport.Context plainAppendSupportContext =
        new ChatTranscriptPlainAppendSupport.Context(
            targetRuntimeCoordinator.docs(),
            styles,
            targetRuntimeCoordinator::ensureTargetExists,
            runtimeFlowCoordinator::breakPresenceRun,
            lineCapSupport::enforceTranscriptLineCap);
    ChatTranscriptPlainSpoilerCoordinator plainSpoilerCoordinator =
        new ChatTranscriptPlainSpoilerCoordinator(
            plainAppendSupportContext, spoilerFlowSupportContext);
    runtimeFlowCoordinator.bindLineLifecycleContexts(
        targetRuntimeCoordinator.docs(),
        targetRuntimeCoordinator.stateByTarget(),
        targetRuntimeCoordinator::ensureTargetExists,
        targetRuntimeCoordinator::noteEpochMs,
        filterRoutingSupport,
        filteredFlowCoordinator::endInsertRun,
        filteredFlowCoordinator::shouldDeferRichTextDuringHistoryBatch,
        documentLineSupport,
        messageLineCoordinator.textAppendSupportContext(),
        messageLineCoordinator.textInsertSupportContext(),
        runtimeSettingsSupport,
        runtimeSettingsSupport::imageEmbedsEnabled,
        runtimeSettingsSupport::linkPreviewsEnabled,
        targetRuntimeCoordinator::newTranscriptState,
        auxiliaryRowsSupport,
        filteredFlowCoordinator::endAppendRun);
    return new Components(
        filteredFlowCoordinator,
        matrixDisplayNameCoordinator,
        replyFlowCoordinator,
        messageInteractionCoordinator,
        messageLineCoordinator,
        plainSpoilerCoordinator,
        runtimeFlowCoordinator,
        targetRuntimeCoordinator);
  }
}
