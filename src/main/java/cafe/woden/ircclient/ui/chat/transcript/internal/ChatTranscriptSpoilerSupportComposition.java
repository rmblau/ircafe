package cafe.woden.ircclient.ui.chat.transcript.internal;

/** Builds reusable support contexts for spoiler-oriented transcript composition. */
final class ChatTranscriptSpoilerSupportComposition {

  private ChatTranscriptSpoilerSupportComposition() {}

  static ChatTranscriptSpoilerSupportComponents create(
      ChatTranscriptSpoilerCompositionInputs inputs) {
    return ChatTranscriptSpoilerSupportComponentsGraphComposition.create(inputs);
  }
}
