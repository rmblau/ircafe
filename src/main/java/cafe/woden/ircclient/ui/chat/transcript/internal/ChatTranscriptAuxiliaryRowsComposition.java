package cafe.woden.ircclient.ui.chat.transcript.internal;

import cafe.woden.ircclient.model.LogDirection;
import cafe.woden.ircclient.model.LogKind;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptAuxiliaryRowsSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptDocumentLineSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptLineMetaSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptRuntimeFlowCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.style.ChatTranscriptStyleRoutingSupport;

/** Builds auxiliary row support for line-oriented transcript composition. */
final class ChatTranscriptAuxiliaryRowsComposition {

  private ChatTranscriptAuxiliaryRowsComposition() {}

  static ChatTranscriptAuxiliaryRowsSupport create(
      ChatStyles styles,
      ChatTranscriptStyleRoutingSupport styleRoutingSupport,
      ChatTranscriptDocumentLineSupport documentLineSupport,
      ChatTranscriptRuntimeFlowCoordinator runtimeFlowCoordinator) {
    return new ChatTranscriptAuxiliaryRowsSupport(
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
  }
}
