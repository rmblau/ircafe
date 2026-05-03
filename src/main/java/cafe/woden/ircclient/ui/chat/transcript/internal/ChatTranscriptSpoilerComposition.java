package cafe.woden.ircclient.ui.chat.transcript.internal;

import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.NickColorService;
import cafe.woden.ircclient.ui.chat.render.ChatRichTextRenderer;
import cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStore;
import cafe.woden.ircclient.ui.chat.transcript.filter.ChatTranscriptFilterRoutingSupport;
import cafe.woden.ircclient.ui.chat.transcript.filter.ChatTranscriptFilteredFlowCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptDocumentLineSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptPlainAppendSupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMatrixDisplayNameCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTimestampFormatter;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptLineCapSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptRuntimeFlowCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptRuntimeSettingsSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptTargetRuntimeCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.spoiler.ChatTranscriptPlainSpoilerCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.spoiler.ChatTranscriptSpoilerAppendSupport;
import cafe.woden.ircclient.ui.chat.transcript.spoiler.ChatTranscriptSpoilerComponentSupport;
import cafe.woden.ircclient.ui.chat.transcript.spoiler.ChatTranscriptSpoilerFlowSupport;
import cafe.woden.ircclient.ui.chat.transcript.spoiler.ChatTranscriptSpoilerHistoryInsertSupport;
import cafe.woden.ircclient.ui.chat.transcript.spoiler.ChatTranscriptSpoilerRevealSupport;
import cafe.woden.ircclient.ui.chat.transcript.spoiler.ChatTranscriptSpoilerRuntimeSupport;
import cafe.woden.ircclient.ui.chat.transcript.spoiler.ChatTranscriptSpoilerWriteSupport;
import cafe.woden.ircclient.ui.chat.transcript.style.ChatTranscriptStyleRoutingSupport;
import cafe.woden.ircclient.ui.settings.UiSettingsBus;

/** Builds spoiler-related transcript collaborators for the store composition. */
final class ChatTranscriptSpoilerComposition {

  record Components(ChatTranscriptPlainSpoilerCoordinator plainSpoilerCoordinator) {}

  private ChatTranscriptSpoilerComposition() {}

  static Components create(
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
        createSpoilerComponentSupportContext(uiSettings, nickColors, matrixDisplayNameCoordinator);
    ChatTranscriptSpoilerWriteSupport.Context spoilerWriteSupportContext =
        createSpoilerWriteSupportContext(
            styles, spoilerComponentSupportContext, styleRoutingSupport);
    ChatTranscriptSpoilerRevealSupport.Context spoilerRevealSupportContext =
        createSpoilerRevealSupportContext(
            styles, renderer, nickColors, matrixDisplayNameCoordinator);
    ChatTranscriptSpoilerRuntimeSupport.Context spoilerRuntimeSupportContext =
        createSpoilerRuntimeSupportContext(
            store, ts, runtimeSettingsSupport, spoilerRevealSupportContext);
    ChatTranscriptSpoilerAppendSupport.Context spoilerAppendSupportContext =
        createSpoilerAppendSupportContext(
            styles, spoilerWriteSupportContext, documentLineSupport, lineCapSupport);
    ChatTranscriptSpoilerHistoryInsertSupport.Context spoilerHistoryInsertSupportContext =
        createSpoilerHistoryInsertSupportContext(
            spoilerWriteSupportContext,
            documentLineSupport,
            runtimeFlowCoordinator,
            lineCapSupport);
    ChatTranscriptSpoilerFlowSupport.Context spoilerFlowSupportContext =
        createSpoilerFlowSupportContext(
            targetRuntimeCoordinator,
            filterRoutingSupport,
            spoilerRuntimeSupportContext,
            spoilerAppendSupportContext,
            spoilerHistoryInsertSupportContext,
            filteredFlowCoordinator);
    ChatTranscriptPlainAppendSupport.Context plainAppendSupportContext =
        createPlainAppendSupportContext(
            styles, runtimeFlowCoordinator, lineCapSupport, targetRuntimeCoordinator);
    return new Components(
        new ChatTranscriptPlainSpoilerCoordinator(
            plainAppendSupportContext, spoilerFlowSupportContext));
  }

  private static ChatTranscriptSpoilerComponentSupport.Context createSpoilerComponentSupportContext(
      UiSettingsBus uiSettings,
      NickColorService nickColors,
      ChatTranscriptMatrixDisplayNameCoordinator matrixDisplayNameCoordinator) {
    return new ChatTranscriptSpoilerComponentSupport.Context(
        uiSettings, nickColors, matrixDisplayNameCoordinator::renderTranscriptFrom);
  }

  private static ChatTranscriptSpoilerWriteSupport.Context createSpoilerWriteSupportContext(
      ChatStyles styles,
      ChatTranscriptSpoilerComponentSupport.Context spoilerComponentSupportContext,
      ChatTranscriptStyleRoutingSupport styleRoutingSupport) {
    return new ChatTranscriptSpoilerWriteSupport.Context(
        styles, spoilerComponentSupportContext, styleRoutingSupport::withFilterMatch);
  }

  private static ChatTranscriptSpoilerRevealSupport.Context createSpoilerRevealSupportContext(
      ChatStyles styles,
      ChatRichTextRenderer renderer,
      NickColorService nickColors,
      ChatTranscriptMatrixDisplayNameCoordinator matrixDisplayNameCoordinator) {
    return new ChatTranscriptSpoilerRevealSupport.Context(
        styles, renderer, nickColors, matrixDisplayNameCoordinator::renderTranscriptFrom);
  }

  private static ChatTranscriptSpoilerRuntimeSupport.Context createSpoilerRuntimeSupportContext(
      ChatTranscriptStore store,
      ChatTimestampFormatter ts,
      ChatTranscriptRuntimeSettingsSupport runtimeSettingsSupport,
      ChatTranscriptSpoilerRevealSupport.Context spoilerRevealSupportContext) {
    return new ChatTranscriptSpoilerRuntimeSupport.Context(
        ts,
        runtimeSettingsSupport::timestampsIncludeChatMessages,
        spoilerRevealSupportContext,
        store);
  }

  private static ChatTranscriptSpoilerAppendSupport.Context createSpoilerAppendSupportContext(
      ChatStyles styles,
      ChatTranscriptSpoilerWriteSupport.Context spoilerWriteSupportContext,
      ChatTranscriptDocumentLineSupport documentLineSupport,
      ChatTranscriptLineCapSupport lineCapSupport) {
    return new ChatTranscriptSpoilerAppendSupport.Context(
        styles,
        spoilerWriteSupportContext,
        documentLineSupport::ensureAtLineStart,
        lineCapSupport::enforceTranscriptLineCap);
  }

  private static ChatTranscriptSpoilerHistoryInsertSupport.Context
      createSpoilerHistoryInsertSupportContext(
          ChatTranscriptSpoilerWriteSupport.Context spoilerWriteSupportContext,
          ChatTranscriptDocumentLineSupport documentLineSupport,
          ChatTranscriptRuntimeFlowCoordinator runtimeFlowCoordinator,
          ChatTranscriptLineCapSupport lineCapSupport) {
    return new ChatTranscriptSpoilerHistoryInsertSupport.Context(
        spoilerWriteSupportContext,
        documentLineSupport::normalizeInsertAtLineStart,
        documentLineSupport::ensureAtLineStartForInsert,
        runtimeFlowCoordinator::shiftCurrentBlock,
        lineCapSupport::enforceTranscriptLineCap);
  }

  private static ChatTranscriptSpoilerFlowSupport.Context createSpoilerFlowSupportContext(
      ChatTranscriptTargetRuntimeCoordinator targetRuntimeCoordinator,
      ChatTranscriptFilterRoutingSupport filterRoutingSupport,
      ChatTranscriptSpoilerRuntimeSupport.Context spoilerRuntimeSupportContext,
      ChatTranscriptSpoilerAppendSupport.Context spoilerAppendSupportContext,
      ChatTranscriptSpoilerHistoryInsertSupport.Context spoilerHistoryInsertSupportContext,
      ChatTranscriptFilteredFlowCoordinator filteredFlowCoordinator) {
    return new ChatTranscriptSpoilerFlowSupport.Context(
        targetRuntimeCoordinator.docs(),
        targetRuntimeCoordinator::ensureTargetExists,
        targetRuntimeCoordinator::noteEpochMs,
        filterRoutingSupport,
        spoilerRuntimeSupportContext,
        spoilerAppendSupportContext,
        spoilerHistoryInsertSupportContext,
        filteredFlowCoordinator::endInsertRun);
  }

  private static ChatTranscriptPlainAppendSupport.Context createPlainAppendSupportContext(
      ChatStyles styles,
      ChatTranscriptRuntimeFlowCoordinator runtimeFlowCoordinator,
      ChatTranscriptLineCapSupport lineCapSupport,
      ChatTranscriptTargetRuntimeCoordinator targetRuntimeCoordinator) {
    return new ChatTranscriptPlainAppendSupport.Context(
        targetRuntimeCoordinator.docs(),
        styles,
        targetRuntimeCoordinator::ensureTargetExists,
        runtimeFlowCoordinator::breakPresenceRun,
        lineCapSupport::enforceTranscriptLineCap);
  }
}
