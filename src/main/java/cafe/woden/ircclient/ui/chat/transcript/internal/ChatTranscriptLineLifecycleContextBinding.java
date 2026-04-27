package cafe.woden.ircclient.ui.chat.transcript.internal;

import cafe.woden.ircclient.ui.chat.transcript.filter.ChatTranscriptFilterRoutingSupport;
import cafe.woden.ircclient.ui.chat.transcript.filter.ChatTranscriptFilteredFlowCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptAuxiliaryRowsSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptDocumentLineSupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageLineCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptRuntimeFlowCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptRuntimeSettingsSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptTargetRuntimeCoordinator;

/** Binds visible-line lifecycle runtime context after transcript collaborators exist. */
final class ChatTranscriptLineLifecycleContextBinding {

  private ChatTranscriptLineLifecycleContextBinding() {}

  static void bind(
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
