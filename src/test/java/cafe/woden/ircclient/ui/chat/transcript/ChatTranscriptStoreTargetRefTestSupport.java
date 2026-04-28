package cafe.woden.ircclient.ui.chat.transcript;

import cafe.woden.ircclient.model.TargetRef;

/** Common transcript target refs used by ChatTranscriptStore tests. */
final class ChatTranscriptStoreTargetRefTestSupport {

  private ChatTranscriptStoreTargetRefTestSupport() {}

  static TargetRef channelRef() {
    return new TargetRef("srv", "#chan");
  }

  static TargetRef statusRef() {
    return new TargetRef("srv", "status");
  }

  static TargetRef matrixRoomRef() {
    return new TargetRef("matrix", "#ircafe:matrix.example.org");
  }
}
