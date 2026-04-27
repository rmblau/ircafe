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

/** Builds reusable line support collaborators for transcript composition. */
final class ChatTranscriptLineSupportComposition {

  record Components(
      ChatTranscriptDocumentLineSupport documentLineSupport,
      ChatTranscriptPresenceFoldSupport presenceFoldSupport,
      ChatTranscriptAuxiliaryRowsSupport auxiliaryRowsSupport) {}

  private ChatTranscriptLineSupportComposition() {}

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
        ChatTranscriptPresenceFoldComposition.create(
            styles, renderer, ts, styleRoutingSupport, documentLineSupport, lineCapSupport);
    ChatTranscriptAuxiliaryRowsSupport auxiliaryRowsSupport =
        ChatTranscriptAuxiliaryRowsComposition.create(
            styles, styleRoutingSupport, documentLineSupport, runtimeFlowCoordinator);
    return new Components(documentLineSupport, presenceFoldSupport, auxiliaryRowsSupport);
  }
}
