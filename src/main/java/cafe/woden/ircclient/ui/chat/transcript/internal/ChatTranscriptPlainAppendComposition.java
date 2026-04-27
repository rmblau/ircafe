package cafe.woden.ircclient.ui.chat.transcript.internal;

import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptPlainAppendSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptLineCapSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptRuntimeFlowCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptTargetRuntimeCoordinator;

/** Builds plain-line append context for spoiler-oriented transcript composition. */
final class ChatTranscriptPlainAppendComposition {

  private ChatTranscriptPlainAppendComposition() {}

  static ChatTranscriptPlainAppendSupport.Context create(
      ChatStyles styles,
      ChatTranscriptRuntimeFlowCoordinator runtimeFlowCoordinator,
      ChatTranscriptLineCapSupport lineCapSupport,
      ChatTranscriptTargetRuntimeCoordinator targetRuntimeCoordinator) {
    return new ChatTranscriptPlainAppendSupport.Context(
        targetRuntimeCoordinator.docs(),
        styles,
        targetRuntimeCoordinator::ensureTargetExists,
        runtimeFlowCoordinator::breakPresenceRun,
        lineCapSupport::enforceTranscriptLineCap);
  }
}
