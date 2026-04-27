package cafe.woden.ircclient.ui.chat.transcript.internal;

import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptPlainAppendSupport;
import cafe.woden.ircclient.ui.chat.transcript.spoiler.ChatTranscriptSpoilerFlowSupport;

/** Builds reusable support contexts for spoiler-oriented transcript composition. */
final class ChatTranscriptSpoilerSupportComposition {

  record Components(
      ChatTranscriptPlainAppendSupport.Context plainAppendSupportContext,
      ChatTranscriptSpoilerFlowSupport.Context spoilerFlowSupportContext) {}

  private ChatTranscriptSpoilerSupportComposition() {}

  static Components create(ChatTranscriptSpoilerCompositionInputs inputs) {
    ChatTranscriptSpoilerSupportContextGraphComposition.Contexts supportContexts =
        ChatTranscriptSpoilerSupportContextGraphComposition.create(inputs);
    return ChatTranscriptSpoilerSupportComponentsComposition.create(
        supportContexts.plainAppendSupportContext(),
        supportContexts.spoilerFlowSupportContext());
  }
}
