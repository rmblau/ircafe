package cafe.woden.ircclient.ui.chat.transcript.internal;

import cafe.woden.ircclient.ui.chat.transcript.spoiler.ChatTranscriptPlainSpoilerCoordinator;

/** Builds the plain spoiler coordinator from spoiler support contexts. */
final class ChatTranscriptPlainSpoilerCoordinatorComposition {

  private ChatTranscriptPlainSpoilerCoordinatorComposition() {}

  static ChatTranscriptPlainSpoilerCoordinator create(
      ChatTranscriptSpoilerSupportComposition.Components spoilerSupportComposition) {
    return new ChatTranscriptPlainSpoilerCoordinator(
        spoilerSupportComposition.plainAppendSupportContext(),
        spoilerSupportComposition.spoilerFlowSupportContext());
  }
}
