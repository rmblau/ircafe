package cafe.woden.ircclient.ui.chat.transcript.internal;

/** Builds the spoiler runtime graph from top-level spoiler composition inputs. */
final class ChatTranscriptSpoilerRuntimeGraphComposition {

  private ChatTranscriptSpoilerRuntimeGraphComposition() {}

  static ChatTranscriptSpoilerRuntimeComposition.Components create(
      ChatTranscriptSpoilerCompositionInputs inputs) {
    return ChatTranscriptSpoilerRuntimeComposition.create(
        inputs.store(),
        inputs.styles(),
        inputs.renderer(),
        inputs.ts(),
        inputs.nickColors(),
        inputs.uiSettings(),
        inputs.matrixDisplayNameCoordinator(),
        inputs.styleRoutingSupport(),
        inputs.runtimeSettingsSupport());
  }
}
