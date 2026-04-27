package cafe.woden.ircclient.ui.chat.transcript.internal;

import cafe.woden.ircclient.irc.roster.UserListPort;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMatrixDisplayNameCoordinator;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptTargetRuntimeCoordinator;
import cafe.woden.ircclient.ui.settings.UiSettingsBus;

/** Builds message display-name collaborators for message-oriented transcript composition. */
final class ChatTranscriptMessageDisplayNameComposition {

  private ChatTranscriptMessageDisplayNameComposition() {}

  static ChatTranscriptMatrixDisplayNameCoordinator create(
      UiSettingsBus uiSettings,
      UserListPort userListStore,
      ChatTranscriptTargetRuntimeCoordinator targetRuntimeCoordinator) {
    return new ChatTranscriptMatrixDisplayNameCoordinator(
        uiSettings, userListStore, targetRuntimeCoordinator.docs());
  }
}
