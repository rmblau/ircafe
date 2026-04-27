package cafe.woden.ircclient.ui.chat.transcript.internal;

/** Builds the spoiler support graph from top-level transcript collaborators. */
final class ChatTranscriptSpoilerSupportGraphComposition {

  private ChatTranscriptSpoilerSupportGraphComposition() {}

  static ChatTranscriptSpoilerSupportComponents create(
      ChatTranscriptSpoilerCompositionInputs inputs) {
    return ChatTranscriptSpoilerSupportComponentsGraphComposition.create(inputs);
  }
}
