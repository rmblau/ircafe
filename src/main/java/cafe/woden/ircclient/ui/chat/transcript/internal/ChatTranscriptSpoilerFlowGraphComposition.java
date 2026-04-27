package cafe.woden.ircclient.ui.chat.transcript.internal;

import cafe.woden.ircclient.ui.chat.transcript.spoiler.ChatTranscriptSpoilerFlowSupport;

/** Builds the spoiler flow graph from top-level spoiler composition inputs. */
final class ChatTranscriptSpoilerFlowGraphComposition {

  private ChatTranscriptSpoilerFlowGraphComposition() {}

  static ChatTranscriptSpoilerFlowSupport.Context create(
      ChatTranscriptSpoilerCompositionInputs inputs,
      ChatTranscriptSpoilerRuntimeComposition.Components spoilerRuntimeComposition) {
    return ChatTranscriptSpoilerFlowComposition.create(
        inputs.styles(),
        spoilerRuntimeComposition,
        inputs.documentLineSupport(),
        inputs.filterRoutingSupport(),
        inputs.runtimeFlowCoordinator(),
        inputs.lineCapSupport(),
        inputs.targetRuntimeCoordinator(),
        inputs.filteredFlowCoordinator());
  }
}
