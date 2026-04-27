package cafe.woden.ircclient.ui.chat.transcript.internal;

import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.transcript.filter.ChatTranscriptFilterRoutingSupport;
import cafe.woden.ircclient.ui.chat.transcript.filter.ChatTranscriptFilteredFlowCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptDocumentLineSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptLineCapSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptRuntimeFlowCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptRuntimeSettingsSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptTargetRuntimeCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.style.ChatTranscriptStyleRoutingSupport;
import cafe.woden.ircclient.ui.filter.FilterEngine;

/** Builds filter-oriented collaborators for the transcript store composition. */
final class ChatTranscriptFilterComposition {

  record Components(
      ChatTranscriptFilteredFlowCoordinator filteredFlowCoordinator,
      ChatTranscriptFilterRoutingSupport filterRoutingSupport) {}

  private ChatTranscriptFilterComposition() {}

  static Components create(
      ChatStyles styles,
      FilterEngine filterEngine,
      ChatTranscriptStyleRoutingSupport styleRoutingSupport,
      ChatTranscriptDocumentLineSupport documentLineSupport,
      ChatTranscriptRuntimeFlowCoordinator runtimeFlowCoordinator,
      ChatTranscriptLineCapSupport lineCapSupport,
      ChatTranscriptTargetRuntimeCoordinator targetRuntimeCoordinator,
      ChatTranscriptRuntimeSettingsSupport runtimeSettingsSupport) {
    ChatTranscriptFilterSupportComposition.Components filterSupportComposition =
        ChatTranscriptFilterSupportComposition.create(
            styles,
            styleRoutingSupport,
            documentLineSupport,
            runtimeFlowCoordinator,
            lineCapSupport);
    ChatTranscriptFilteredFlowCoordinator filteredFlowCoordinator =
        new ChatTranscriptFilteredFlowCoordinator(filterSupportComposition.filteredLinesSupport());
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
    return new Components(filteredFlowCoordinator, filterRoutingSupport);
  }
}
