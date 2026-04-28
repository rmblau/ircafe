package cafe.woden.ircclient.ui.chat.transcript;

import cafe.woden.ircclient.irc.IrcEvent.AccountState;
import cafe.woden.ircclient.irc.IrcEvent.AwayState;
import cafe.woden.ircclient.irc.IrcEvent.NickInfo;
import cafe.woden.ircclient.irc.roster.UserListStore;
import java.util.List;

/** Shared Matrix roster fixtures for {@link ChatTranscriptStoreTest}. */
final class ChatTranscriptStoreMatrixTestSupport {

  static final String MATRIX_SERVER = "matrix";
  static final String MATRIX_ROOM = "#ircafe:matrix.example.org";
  static final String MATRIX_ALICE_USER_ID = "@alice:matrix.example.org";
  static final String MATRIX_BRIDGED_WODENCAFE_USER_ID =
      "@irc_libera_wodencafe:matrix.zimmedon.com";

  private ChatTranscriptStoreMatrixTestSupport() {}

  static UserListStore userListWithMatrixDisplayName(String userId, String displayName) {
    UserListStore userListStore = new UserListStore();
    putMatrixNick(userListStore, userId);
    userListStore.updateRealNameAcrossChannels(MATRIX_SERVER, userId, displayName);
    return userListStore;
  }

  static void putMatrixNick(UserListStore userListStore, String userId) {
    userListStore.put(MATRIX_SERVER, MATRIX_ROOM, List.of(new NickInfo(userId, "", "")));
  }

  static void putMatrixBridgedNick(
      UserListStore userListStore, String userId, String displayName) {
    userListStore.put(
        MATRIX_SERVER,
        MATRIX_ROOM,
        List.of(
            new NickInfo(
                userId, "", "", AwayState.UNKNOWN, null, AccountState.UNKNOWN, null, displayName)));
  }
}
