package cafe.woden.ircclient.ui.chat.transcript;

import cafe.woden.ircclient.logging.history.LoadOlderControlState;
import cafe.woden.ircclient.ui.chat.fold.LoadOlderMessagesComponent;

/** Maps history load-older port state into the transcript UI control state. */
final class ChatHistoryTranscriptLoadOlderStateMapper {

  private ChatHistoryTranscriptLoadOlderStateMapper() {}

  static LoadOlderMessagesComponent.State toUiState(LoadOlderControlState state) {
    if (state == null) return LoadOlderMessagesComponent.State.READY;
    return switch (state) {
      case READY -> LoadOlderMessagesComponent.State.READY;
      case LOADING -> LoadOlderMessagesComponent.State.LOADING;
      case EXHAUSTED -> LoadOlderMessagesComponent.State.EXHAUSTED;
      case UNAVAILABLE -> LoadOlderMessagesComponent.State.UNAVAILABLE;
    };
  }
}
