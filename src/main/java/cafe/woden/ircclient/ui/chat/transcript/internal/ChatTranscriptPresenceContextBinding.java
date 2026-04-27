package cafe.woden.ircclient.ui.chat.transcript.internal;

import cafe.woden.ircclient.ui.chat.transcript.filter.ChatTranscriptFilterRoutingSupport;
import cafe.woden.ircclient.ui.chat.transcript.filter.ChatTranscriptFilteredFlowCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptPresenceFoldSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptRuntimeFlowCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptRuntimeSettingsSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptTargetRuntimeCoordinator;

/** Binds presence-flow runtime context after transcript collaborators exist. */
final class ChatTranscriptPresenceContextBinding {

  private ChatTranscriptPresenceContextBinding() {}

  static void bind(
      ChatTranscriptRuntimeFlowCoordinator runtimeFlowCoordinator,
      ChatTranscriptPresenceFoldSupport presenceFoldSupport,
      ChatTranscriptFilterRoutingSupport filterRoutingSupport,
      ChatTranscriptFilteredFlowCoordinator filteredFlowCoordinator,
      ChatTranscriptRuntimeSettingsSupport runtimeSettingsSupport,
      ChatTranscriptTargetRuntimeCoordinator targetRuntimeCoordinator) {
    runtimeFlowCoordinator.bindPresenceContext(
        presenceFoldSupport,
        filterRoutingSupport,
        filteredFlowCoordinator.filteredLinesSupport(),
        runtimeSettingsSupport,
        targetRuntimeCoordinator.docs(),
        targetRuntimeCoordinator.stateByTarget(),
        targetRuntimeCoordinator::ensureTargetExists,
        targetRuntimeCoordinator::noteEpochMs,
        System::currentTimeMillis);
  }
}
