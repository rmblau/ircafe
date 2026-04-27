package cafe.woden.ircclient.ui.chat.transcript.internal;

import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.transcript.filter.ChatTranscriptFilteredLinesSupport;
import cafe.woden.ircclient.ui.chat.transcript.filter.ChatTranscriptFilteredRunSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptDocumentLineSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptLineMetaSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptLineCapSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptRuntimeFlowCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.style.ChatTranscriptStyleRoutingSupport;

/** Builds reusable support collaborators for filter-oriented transcript composition. */
final class ChatTranscriptFilterSupportComposition {

  record Components(ChatTranscriptFilteredLinesSupport filteredLinesSupport) {}

  private ChatTranscriptFilterSupportComposition() {}

  static Components create(
      ChatStyles styles,
      ChatTranscriptStyleRoutingSupport styleRoutingSupport,
      ChatTranscriptDocumentLineSupport documentLineSupport,
      ChatTranscriptRuntimeFlowCoordinator runtimeFlowCoordinator,
      ChatTranscriptLineCapSupport lineCapSupport) {
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
    return new Components(filteredLinesSupport);
  }
}
