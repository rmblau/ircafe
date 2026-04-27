package cafe.woden.ircclient.ui.chat.transcript.internal;

import cafe.woden.ircclient.irc.roster.UserListPort;
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
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptPresenceFoldSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptPlainAppendSupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMatrixDisplayNameCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageCatalogSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTimestampFormatter;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptLineCapSupport;
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

/** Wires the transcript helper graph used by {@link ChatTranscriptStore}. */
public final class ChatTranscriptStoreComposition {

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

    ChatTranscriptRuntimeComposition.Components runtimeComposition =
        ChatTranscriptRuntimeComposition.create(
            store, styles, nickColors, nickColorSettings, uiSettings);
    ChatTranscriptRuntimeFlowCoordinator runtimeFlowCoordinator =
        runtimeComposition.runtimeFlowCoordinator();
    ChatTranscriptRuntimeSettingsSupport runtimeSettingsSupport =
        runtimeComposition.runtimeSettingsSupport();
    ChatTranscriptStyleRoutingSupport styleRoutingSupport =
        runtimeComposition.styleRoutingSupport();
    ChatTranscriptLineCapSupport lineCapSupport = runtimeComposition.lineCapSupport();
    ChatTranscriptMessageCatalogSupport messageCatalogSupport =
        runtimeComposition.messageCatalogSupport();
    ChatTranscriptTargetRuntimeCoordinator targetRuntimeCoordinator =
        runtimeComposition.targetRuntimeCoordinator();

    ChatTranscriptLineComposition.Components lineComposition =
        ChatTranscriptLineComposition.create(
            styles, renderer, ts, styleRoutingSupport, runtimeFlowCoordinator, lineCapSupport);
    ChatTranscriptDocumentLineSupport documentLineSupport = lineComposition.documentLineSupport();
    FilterComposition filterComposition =
        createFilterComposition(
            styles,
            filterEngine,
            styleRoutingSupport,
            documentLineSupport,
            runtimeFlowCoordinator,
            lineCapSupport,
            targetRuntimeCoordinator,
            runtimeSettingsSupport);
    ChatTranscriptFilteredFlowCoordinator filteredFlowCoordinator =
        filterComposition.filteredFlowCoordinator();
    ChatTranscriptFilterRoutingSupport filterRoutingSupport =
        filterComposition.filterRoutingSupport();
    ChatTranscriptPresenceFoldSupport presenceFoldSupport = lineComposition.presenceFoldSupport();
    ChatTranscriptMatrixDisplayNameCoordinator matrixDisplayNameCoordinator =
        new ChatTranscriptMatrixDisplayNameCoordinator(
            uiSettings, userListStore, targetRuntimeCoordinator.docs());
    SpoilerComposition spoilerComposition =
        createSpoilerComposition(
            store,
            styles,
            renderer,
            ts,
            nickColors,
            uiSettings,
            matrixDisplayNameCoordinator,
            documentLineSupport,
            styleRoutingSupport,
            filterRoutingSupport,
            runtimeFlowCoordinator,
            lineCapSupport,
            targetRuntimeCoordinator,
            runtimeSettingsSupport,
            filteredFlowCoordinator);
    ChatTranscriptPlainSpoilerCoordinator plainSpoilerCoordinator =
        spoilerComposition.plainSpoilerCoordinator();
    ChatTranscriptAuxiliaryRowsSupport auxiliaryRowsSupport =
        lineComposition.auxiliaryRowsSupport();
    ChatTranscriptMessageComposition.Components messageComposition =
        ChatTranscriptMessageComposition.create(
            store,
            styles,
            renderer,
            ts,
            nickColors,
            imageEmbeds,
            linkPreviews,
            matrixDisplayNameCoordinator,
            styleRoutingSupport,
            filterRoutingSupport,
            documentLineSupport,
            lineCapSupport,
            runtimeFlowCoordinator,
            targetRuntimeCoordinator,
            runtimeSettingsSupport,
            messageCatalogSupport,
            filteredFlowCoordinator);
    ChatTranscriptReplyFlowCoordinator replyFlowCoordinator =
        messageComposition.replyFlowCoordinator();
    ChatTranscriptMessageLineCoordinator messageLineCoordinator =
        messageComposition.messageLineCoordinator();
    ChatTranscriptMessageInteractionCoordinator messageInteractionCoordinator =
        messageComposition.messageInteractionCoordinator();
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

  private record SpoilerComposition(
      ChatTranscriptPlainSpoilerCoordinator plainSpoilerCoordinator) {}

  private static SpoilerComposition createSpoilerComposition(
      ChatTranscriptStore store,
      ChatStyles styles,
      ChatRichTextRenderer renderer,
      ChatTimestampFormatter ts,
      NickColorService nickColors,
      UiSettingsBus uiSettings,
      ChatTranscriptMatrixDisplayNameCoordinator matrixDisplayNameCoordinator,
      ChatTranscriptDocumentLineSupport documentLineSupport,
      ChatTranscriptStyleRoutingSupport styleRoutingSupport,
      ChatTranscriptFilterRoutingSupport filterRoutingSupport,
      ChatTranscriptRuntimeFlowCoordinator runtimeFlowCoordinator,
      ChatTranscriptLineCapSupport lineCapSupport,
      ChatTranscriptTargetRuntimeCoordinator targetRuntimeCoordinator,
      ChatTranscriptRuntimeSettingsSupport runtimeSettingsSupport,
      ChatTranscriptFilteredFlowCoordinator filteredFlowCoordinator) {
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
    ChatTranscriptPlainAppendSupport.Context plainAppendSupportContext =
        new ChatTranscriptPlainAppendSupport.Context(
            targetRuntimeCoordinator.docs(),
            styles,
            targetRuntimeCoordinator::ensureTargetExists,
            runtimeFlowCoordinator::breakPresenceRun,
            lineCapSupport::enforceTranscriptLineCap);
    return new SpoilerComposition(
        new ChatTranscriptPlainSpoilerCoordinator(
            plainAppendSupportContext, spoilerFlowSupportContext));
  }

  private record FilterComposition(
      ChatTranscriptFilteredFlowCoordinator filteredFlowCoordinator,
      ChatTranscriptFilterRoutingSupport filterRoutingSupport) {}

  private static FilterComposition createFilterComposition(
      ChatStyles styles,
      FilterEngine filterEngine,
      ChatTranscriptStyleRoutingSupport styleRoutingSupport,
      ChatTranscriptDocumentLineSupport documentLineSupport,
      ChatTranscriptRuntimeFlowCoordinator runtimeFlowCoordinator,
      ChatTranscriptLineCapSupport lineCapSupport,
      ChatTranscriptTargetRuntimeCoordinator targetRuntimeCoordinator,
      ChatTranscriptRuntimeSettingsSupport runtimeSettingsSupport) {
    ChatTranscriptFilteredRunSupport.Context filteredRunSupportContext =
        new ChatTranscriptFilteredRunSupport.Context(styles, ChatTranscriptLineMetaSupport::bind);
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
    return new FilterComposition(filteredFlowCoordinator, filterRoutingSupport);
  }
}
