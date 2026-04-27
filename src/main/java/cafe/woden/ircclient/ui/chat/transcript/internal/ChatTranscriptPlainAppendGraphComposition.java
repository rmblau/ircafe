package cafe.woden.ircclient.ui.chat.transcript.internal;

import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptPlainAppendSupport;

/** Builds the plain append graph from top-level spoiler composition inputs. */
final class ChatTranscriptPlainAppendGraphComposition {

  private ChatTranscriptPlainAppendGraphComposition() {}

  static ChatTranscriptPlainAppendSupport.Context create(
      ChatTranscriptSpoilerCompositionInputs inputs) {
    return ChatTranscriptPlainAppendComposition.create(
        inputs.styles(),
        inputs.runtimeFlowCoordinator(),
        inputs.lineCapSupport(),
        inputs.targetRuntimeCoordinator());
  }
}
