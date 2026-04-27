package cafe.woden.ircclient.ui.chat.transcript.internal;

/** Builds spoiler support components from top-level spoiler composition inputs. */
final class ChatTranscriptSpoilerSupportComponentsGraphComposition {

  private ChatTranscriptSpoilerSupportComponentsGraphComposition() {}

  static ChatTranscriptSpoilerSupportComposition.Components create(
      ChatTranscriptSpoilerCompositionInputs inputs) {
    ChatTranscriptSpoilerSupportContextGraphComposition.Contexts supportContexts =
        ChatTranscriptSpoilerSupportContextGraphComposition.create(inputs);
    return ChatTranscriptSpoilerSupportComponentsComposition.create(
        supportContexts.plainAppendSupportContext(),
        supportContexts.spoilerFlowSupportContext());
  }
}
