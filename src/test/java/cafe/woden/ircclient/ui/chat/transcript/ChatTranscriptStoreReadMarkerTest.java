package cafe.woden.ircclient.ui.chat.transcript;

import static cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStoreTargetRefTestSupport.channelRef;
import static cafe.woden.ircclient.ui.chat.transcript.ChatTranscriptStoreTestFactory.newStore;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.model.TargetRef;
import org.junit.jupiter.api.Test;

class ChatTranscriptStoreReadMarkerTest {

  @Test
  void readMarkerPersistsWhenSetBeforeUnreadLinesExist() {
    ChatTranscriptStore store = newStore();
    TargetRef ref = channelRef();

    store.updateReadMarker(ref, 1_000L);
    assertEquals(-1, store.readMarkerJumpOffset(ref));

    store.appendChatAt(ref, "alice", "older", false, 900L);
    assertEquals(-1, store.readMarkerJumpOffset(ref));

    store.appendChatAt(ref, "alice", "newer", false, 1_100L);
    assertTrue(store.readMarkerJumpOffset(ref) >= 0);
  }

  @Test
  void clearReadMarkersForServerRemovesMarkerStateWithoutAffectingOtherServers() {
    ChatTranscriptStore store = newStore();
    TargetRef onServer = channelRef();
    TargetRef otherServer = new TargetRef("other", "#chan");

    store.appendChatAt(onServer, "alice", "older", false, 900L);
    store.appendChatAt(onServer, "alice", "newer", false, 1_100L);
    store.updateReadMarker(onServer, 1_000L);
    assertTrue(store.readMarkerJumpOffset(onServer) >= 0);

    store.appendChatAt(otherServer, "alice", "older", false, 900L);
    store.appendChatAt(otherServer, "alice", "newer", false, 1_100L);
    store.updateReadMarker(otherServer, 1_000L);
    assertTrue(store.readMarkerJumpOffset(otherServer) >= 0);

    store.clearReadMarkersForServer("srv");

    assertEquals(-1, store.readMarkerJumpOffset(onServer));
    assertTrue(store.readMarkerJumpOffset(otherServer) >= 0);

    store.appendChatAt(onServer, "alice", "latest", false, 1_200L);
    assertEquals(-1, store.readMarkerJumpOffset(onServer));
  }
}
