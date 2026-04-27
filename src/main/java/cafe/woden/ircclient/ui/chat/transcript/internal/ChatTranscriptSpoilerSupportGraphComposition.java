package cafe.woden.ircclient.ui.chat.transcript.internal;

/** Builds the spoiler support graph from top-level transcript collaborators. */
final class ChatTranscriptSpoilerSupportGraphComposition {

  private ChatTranscriptSpoilerSupportGraphComposition() {}

  static ChatTranscriptSpoilerSupportComposition.Components create(
      ChatTranscriptSpoilerCompositionInputs inputs) {
    return ChatTranscriptSpoilerSupportComposition.create(
        inputs.store(),
        inputs.styles(),
        inputs.renderer(),
        inputs.ts(),
        inputs.nickColors(),
        inputs.uiSettings(),
        inputs.matrixDisplayNameCoordinator(),
        inputs.documentLineSupport(),
        inputs.styleRoutingSupport(),
        inputs.filterRoutingSupport(),
        inputs.runtimeFlowCoordinator(),
        inputs.lineCapSupport(),
        inputs.targetRuntimeCoordinator(),
        inputs.runtimeSettingsSupport(),
        inputs.filteredFlowCoordinator());
  }
}
