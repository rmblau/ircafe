package cafe.woden.ircclient.ui.chat.transcript.internal;

import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptPlainAppendSupport;
import cafe.woden.ircclient.ui.chat.transcript.spoiler.ChatTranscriptSpoilerFlowSupport;

/** Builds assembled spoiler support contexts from top-level spoiler composition inputs. */
final class ChatTranscriptSpoilerSupportContextGraphComposition {

  record Contexts(
      ChatTranscriptPlainAppendSupport.Context plainAppendSupportContext,
      ChatTranscriptSpoilerFlowSupport.Context spoilerFlowSupportContext) {}

  private ChatTranscriptSpoilerSupportContextGraphComposition() {}

  static Contexts create(ChatTranscriptSpoilerCompositionInputs inputs) {
    ChatTranscriptSpoilerRuntimeComposition.Components spoilerRuntimeComposition =
        ChatTranscriptSpoilerRuntimeGraphComposition.create(inputs);
    ChatTranscriptSpoilerFlowSupport.Context spoilerFlowSupportContext =
        ChatTranscriptSpoilerFlowGraphComposition.create(inputs, spoilerRuntimeComposition);
    ChatTranscriptPlainAppendSupport.Context plainAppendSupportContext =
        ChatTranscriptPlainAppendGraphComposition.create(inputs);
    return new Contexts(plainAppendSupportContext, spoilerFlowSupportContext);
  }
}
