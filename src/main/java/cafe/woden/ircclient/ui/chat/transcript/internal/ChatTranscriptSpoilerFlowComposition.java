package cafe.woden.ircclient.ui.chat.transcript.internal;

import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.transcript.filter.ChatTranscriptFilterRoutingSupport;
import cafe.woden.ircclient.ui.chat.transcript.filter.ChatTranscriptFilteredFlowCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptDocumentLineSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptLineCapSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptRuntimeFlowCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptTargetRuntimeCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.spoiler.ChatTranscriptSpoilerAppendSupport;
import cafe.woden.ircclient.ui.chat.transcript.spoiler.ChatTranscriptSpoilerFlowSupport;
import cafe.woden.ircclient.ui.chat.transcript.spoiler.ChatTranscriptSpoilerHistoryInsertSupport;

/** Builds spoiler append/history flow support for spoiler-oriented transcript composition. */
final class ChatTranscriptSpoilerFlowComposition {

  private ChatTranscriptSpoilerFlowComposition() {}

  static ChatTranscriptSpoilerFlowSupport.Context create(
      ChatStyles styles,
      ChatTranscriptSpoilerRuntimeComposition.Components spoilerRuntimeComposition,
      ChatTranscriptDocumentLineSupport documentLineSupport,
      ChatTranscriptFilterRoutingSupport filterRoutingSupport,
      ChatTranscriptRuntimeFlowCoordinator runtimeFlowCoordinator,
      ChatTranscriptLineCapSupport lineCapSupport,
      ChatTranscriptTargetRuntimeCoordinator targetRuntimeCoordinator,
      ChatTranscriptFilteredFlowCoordinator filteredFlowCoordinator) {
    ChatTranscriptSpoilerAppendSupport.Context spoilerAppendSupportContext =
        new ChatTranscriptSpoilerAppendSupport.Context(
            styles,
            spoilerRuntimeComposition.spoilerWriteSupportContext(),
            documentLineSupport::ensureAtLineStart,
            lineCapSupport::enforceTranscriptLineCap);
    ChatTranscriptSpoilerHistoryInsertSupport.Context spoilerHistoryInsertSupportContext =
        new ChatTranscriptSpoilerHistoryInsertSupport.Context(
            spoilerRuntimeComposition.spoilerWriteSupportContext(),
            documentLineSupport::normalizeInsertAtLineStart,
            documentLineSupport::ensureAtLineStartForInsert,
            runtimeFlowCoordinator::shiftCurrentBlock,
            lineCapSupport::enforceTranscriptLineCap);
    return new ChatTranscriptSpoilerFlowSupport.Context(
        targetRuntimeCoordinator.docs(),
        targetRuntimeCoordinator::ensureTargetExists,
        targetRuntimeCoordinator::noteEpochMs,
        filterRoutingSupport,
        spoilerRuntimeComposition.spoilerRuntimeSupportContext(),
        spoilerAppendSupportContext,
        spoilerHistoryInsertSupportContext,
        filteredFlowCoordinator::endInsertRun);
  }
}
