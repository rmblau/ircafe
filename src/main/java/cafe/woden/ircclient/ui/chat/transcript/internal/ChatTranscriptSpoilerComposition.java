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
import cafe.woden.ircclient.ui.chat.transcript.spoiler.ChatTranscriptSpoilerFlowSupport;
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
    ChatTranscriptSpoilerRuntimeComposition.Components spoilerRuntimeComposition =
        ChatTranscriptSpoilerRuntimeComposition.create(
            store,
            styles,
            renderer,
            ts,
            nickColors,
            uiSettings,
            matrixDisplayNameCoordinator,
            styleRoutingSupport,
            runtimeSettingsSupport);
    ChatTranscriptSpoilerFlowSupport.Context spoilerFlowSupportContext =
        ChatTranscriptSpoilerFlowComposition.create(
            styles,
            spoilerRuntimeComposition,
            documentLineSupport,
            filterRoutingSupport,
            runtimeFlowCoordinator,
            lineCapSupport,
            targetRuntimeCoordinator,
            filteredFlowCoordinator);
    ChatTranscriptPlainAppendSupport.Context plainAppendSupportContext =
        new ChatTranscriptPlainAppendSupport.Context(
            targetRuntimeCoordinator.docs(),
            styles,
            targetRuntimeCoordinator::ensureTargetExists,
            runtimeFlowCoordinator::breakPresenceRun,
            lineCapSupport::enforceTranscriptLineCap);
    return new Components(
        new ChatTranscriptPlainSpoilerCoordinator(
            plainAppendSupportContext, spoilerFlowSupportContext));
  }
}
