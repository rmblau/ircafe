package cafe.woden.ircclient.ui.chat.transcript.internal;

import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.render.ChatRichTextRenderer;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptDocumentLineSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptLineMetaSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptPresenceFoldSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTimestampFormatter;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptLineCapSupport;
import cafe.woden.ircclient.ui.chat.transcript.style.ChatTranscriptStyleRoutingSupport;

/** Builds presence-fold support for line-oriented transcript composition. */
final class ChatTranscriptPresenceFoldComposition {

  private ChatTranscriptPresenceFoldComposition() {}

  static ChatTranscriptPresenceFoldSupport create(
      ChatStyles styles,
      ChatRichTextRenderer renderer,
      ChatTimestampFormatter ts,
      ChatTranscriptStyleRoutingSupport styleRoutingSupport,
      ChatTranscriptDocumentLineSupport documentLineSupport,
      ChatTranscriptLineCapSupport lineCapSupport) {
    return new ChatTranscriptPresenceFoldSupport(
        styles,
        renderer,
        ts,
        ChatTranscriptLineMetaSupport::bind,
        ChatTranscriptLineMetaSupport::withExistingMeta,
        styleRoutingSupport::withFilterMatch,
        documentLineSupport::ensureAtLineStart,
        lineCapSupport::enforceTranscriptLineCap);
  }
}
