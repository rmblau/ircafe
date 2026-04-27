package cafe.woden.ircclient.ui.chat.transcript.internal;

import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptPlainAppendSupport;
import cafe.woden.ircclient.ui.chat.transcript.spoiler.ChatTranscriptSpoilerFlowSupport;

/** Builds spoiler support composition results from assembled support contexts. */
final class ChatTranscriptSpoilerSupportComponentsComposition {

  private ChatTranscriptSpoilerSupportComponentsComposition() {}

  static ChatTranscriptSpoilerSupportComponents create(
      ChatTranscriptPlainAppendSupport.Context plainAppendSupportContext,
      ChatTranscriptSpoilerFlowSupport.Context spoilerFlowSupportContext) {
    return new ChatTranscriptSpoilerSupportComponents(
        plainAppendSupportContext, spoilerFlowSupportContext);
  }
}
