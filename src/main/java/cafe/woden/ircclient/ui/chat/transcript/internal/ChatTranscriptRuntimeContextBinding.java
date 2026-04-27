package cafe.woden.ircclient.ui.chat.transcript.internal;

import cafe.woden.ircclient.ui.chat.transcript.filter.ChatTranscriptFilterRoutingSupport;
import cafe.woden.ircclient.ui.chat.transcript.filter.ChatTranscriptFilteredFlowCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptAuxiliaryRowsSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptDocumentLineSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptPresenceFoldSupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageLineCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptRuntimeFlowCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptRuntimeSettingsSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptTargetRuntimeCoordinator;

/** Binds runtime flow contexts after the transcript helper graph has been assembled. */
final class ChatTranscriptRuntimeContextBinding {

  private ChatTranscriptRuntimeContextBinding() {}

  static void bind(
      ChatTranscriptRuntimeFlowCoordinator runtimeFlowCoordinator,
      ChatTranscriptPresenceFoldSupport presenceFoldSupport,
      ChatTranscriptFilterRoutingSupport filterRoutingSupport,
      ChatTranscriptFilteredFlowCoordinator filteredFlowCoordinator,
      ChatTranscriptRuntimeSettingsSupport runtimeSettingsSupport,
      ChatTranscriptTargetRuntimeCoordinator targetRuntimeCoordinator,
      ChatTranscriptDocumentLineSupport documentLineSupport,
      ChatTranscriptMessageLineCoordinator messageLineCoordinator,
      ChatTranscriptAuxiliaryRowsSupport auxiliaryRowsSupport) {
    bindPresenceContext(
        runtimeFlowCoordinator,
        presenceFoldSupport,
        filterRoutingSupport,
        filteredFlowCoordinator,
        runtimeSettingsSupport,
        targetRuntimeCoordinator);
    bindLineLifecycleContexts(
        runtimeFlowCoordinator,
        filterRoutingSupport,
        filteredFlowCoordinator,
        runtimeSettingsSupport,
        targetRuntimeCoordinator,
        documentLineSupport,
        messageLineCoordinator,
        auxiliaryRowsSupport);
  }

  private static void bindPresenceContext(
      ChatTranscriptRuntimeFlowCoordinator runtimeFlowCoordinator,
      ChatTranscriptPresenceFoldSupport presenceFoldSupport,
      ChatTranscriptFilterRoutingSupport filterRoutingSupport,
      ChatTranscriptFilteredFlowCoordinator filteredFlowCoordinator,
      ChatTranscriptRuntimeSettingsSupport runtimeSettingsSupport,
      ChatTranscriptTargetRuntimeCoordinator targetRuntimeCoordinator) {
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
  }

  private static void bindLineLifecycleContexts(
      ChatTranscriptRuntimeFlowCoordinator runtimeFlowCoordinator,
      ChatTranscriptFilterRoutingSupport filterRoutingSupport,
      ChatTranscriptFilteredFlowCoordinator filteredFlowCoordinator,
      ChatTranscriptRuntimeSettingsSupport runtimeSettingsSupport,
      ChatTranscriptTargetRuntimeCoordinator targetRuntimeCoordinator,
      ChatTranscriptDocumentLineSupport documentLineSupport,
      ChatTranscriptMessageLineCoordinator messageLineCoordinator,
      ChatTranscriptAuxiliaryRowsSupport auxiliaryRowsSupport) {
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
  }
}
