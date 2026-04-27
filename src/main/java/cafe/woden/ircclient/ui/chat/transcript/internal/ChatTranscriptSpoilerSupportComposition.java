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
    ChatTranscriptSpoilerRuntimeComposition.Components spoilerRuntimeComposition =
        ChatTranscriptSpoilerRuntimeGraphComposition.create(inputs);
    ChatTranscriptSpoilerFlowSupport.Context spoilerFlowSupportContext =
        ChatTranscriptSpoilerFlowComposition.create(
            inputs.styles(),
            spoilerRuntimeComposition,
            inputs.documentLineSupport(),
            inputs.filterRoutingSupport(),
            inputs.runtimeFlowCoordinator(),
            inputs.lineCapSupport(),
            inputs.targetRuntimeCoordinator(),
            inputs.filteredFlowCoordinator());
    ChatTranscriptPlainAppendSupport.Context plainAppendSupportContext =
        ChatTranscriptPlainAppendGraphComposition.create(inputs);
    return ChatTranscriptSpoilerSupportComponentsComposition.create(
        plainAppendSupportContext, spoilerFlowSupportContext);
  }
}
