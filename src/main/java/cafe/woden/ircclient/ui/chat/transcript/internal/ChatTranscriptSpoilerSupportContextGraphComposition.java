package cafe.woden.ircclient.ui.chat.transcript.internal;

import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptPlainAppendSupport;
import cafe.woden.ircclient.ui.chat.transcript.spoiler.ChatTranscriptSpoilerFlowSupport;

/** Builds assembled spoiler support contexts from top-level spoiler composition inputs. */
final class ChatTranscriptSpoilerSupportContextGraphComposition {

  record Contexts(
      ChatTranscriptPlainAppendSupport.Context plainAppendSupportContext,
      ChatTranscriptSpoilerFlowSupport.Context spoilerFlowSupportContext) {}

  private ChatTranscriptSpoilerSupportContextGraphComposition() {}

  static Contexts create(ChatTranscriptSpoilerCompositionInputs inputs) {
    ChatTranscriptSpoilerRuntimeComposition.Components spoilerRuntimeComposition =
        ChatTranscriptSpoilerRuntimeComposition.create(
            inputs.store(),
            inputs.styles(),
            inputs.renderer(),
            inputs.ts(),
            inputs.nickColors(),
            inputs.uiSettings(),
            inputs.matrixDisplayNameCoordinator(),
            inputs.styleRoutingSupport(),
            inputs.runtimeSettingsSupport());
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
        ChatTranscriptPlainAppendComposition.create(
            inputs.styles(),
            inputs.runtimeFlowCoordinator(),
            inputs.lineCapSupport(),
            inputs.targetRuntimeCoordinator());
    return new Contexts(plainAppendSupportContext, spoilerFlowSupportContext);
  }
}
