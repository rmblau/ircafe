package cafe.woden.ircclient.ui.chat.transcript.support;

import cafe.woden.ircclient.model.TargetRef;

/** Common transcript target refs used by ChatTranscriptStore tests. */
public final class ChatTranscriptStoreTargetRefTestSupport {

  private ChatTranscriptStoreTargetRefTestSupport() {}

  public static TargetRef channelRef() {
    return new TargetRef("srv", "#chan");
  }

  public static TargetRef statusRef() {
    return new TargetRef("srv", "status");
  }

  public static TargetRef matrixRoomRef() {
    return new TargetRef("matrix", "#ircafe:matrix.example.org");
  }
}
