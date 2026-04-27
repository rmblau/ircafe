package cafe.woden.ircclient.ui.chat.transcript.internal;

import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.render.ChatRichTextRenderer;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptAuxiliaryRowsSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptDocumentLineSupport;
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
    ChatTranscriptLineSupportComposition.Components lineSupportComposition =
        ChatTranscriptLineSupportComposition.create(
            styles, renderer, ts, styleRoutingSupport, runtimeFlowCoordinator, lineCapSupport);
    return new Components(
        lineSupportComposition.documentLineSupport(),
        lineSupportComposition.presenceFoldSupport(),
        lineSupportComposition.auxiliaryRowsSupport());
  }
}
