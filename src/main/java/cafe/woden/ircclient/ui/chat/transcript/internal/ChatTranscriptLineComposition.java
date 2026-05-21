package cafe.woden.ircclient.ui.chat.transcript.internal;

import cafe.woden.ircclient.model.LogDirection;
import cafe.woden.ircclient.model.LogKind;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.render.ChatRichTextRenderer;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptAuxiliaryRowsSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptDocumentLineSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptLineMetaSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptPresenceFoldSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTimestampFormatter;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptLineCapSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptRuntimeFlowCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.style.ChatTranscriptStyleRoutingSupport;

/** Builds line-oriented collaborators for the transcript store composition. */
final class ChatTranscriptLineComposition {

  record Components(
      ChatTranscriptDocumentLineSupport documentLineSupport,
      ChatTranscriptPresenceFoldSupport presenceFoldSupport,
      ChatTranscriptAuxiliaryRowsSupport auxiliaryRowsSupport) {}

  private ChatTranscriptLineComposition() {}

  static Components create(
      ChatStyles styles,
      ChatRichTextRenderer renderer,
      ChatTimestampFormatter ts,
      ChatTranscriptStyleRoutingSupport styleRoutingSupport,
      ChatTranscriptRuntimeFlowCoordinator runtimeFlowCoordinator,
      ChatTranscriptLineCapSupport lineCapSupport) {
    ChatTranscriptDocumentLineSupport documentLineSupport =
        new ChatTranscriptDocumentLineSupport(styles);
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
    return new Components(documentLineSupport, presenceFoldSupport, auxiliaryRowsSupport);
  }
}
