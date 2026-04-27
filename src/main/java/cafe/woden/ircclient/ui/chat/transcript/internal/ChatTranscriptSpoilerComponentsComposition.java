package cafe.woden.ircclient.ui.chat.transcript.internal;

/** Builds the spoiler composition result from assembled spoiler support contexts. */
final class ChatTranscriptSpoilerComponentsComposition {

  private ChatTranscriptSpoilerComponentsComposition() {}

  static ChatTranscriptSpoilerComposition.Components create(
      ChatTranscriptSpoilerSupportComposition.Components spoilerSupportComposition) {
    return new ChatTranscriptSpoilerComposition.Components(
        ChatTranscriptPlainSpoilerCoordinatorComposition.create(spoilerSupportComposition));
  }
}
