package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class Ircv3HistoryBootstrapSuppressionPolicyTest {

  @Test
  void suppressesZncPlaybackBootstrapCommandsFromSelf() {
    assertTrue(Ircv3HistoryBootstrapSuppressionPolicy.shouldSuppress(true, "alice", "play * 19"));
    assertTrue(
        Ircv3HistoryBootstrapSuppressionPolicy.shouldSuppress(
            true, "*playback", "play #ircafe 1710000000"));
    assertFalse(
        Ircv3HistoryBootstrapSuppressionPolicy.shouldSuppress(false, "*playback", "play * 19"));
  }

  @Test
  void suppressesZncNetworkDiscoveryStatusCommand() {
    assertTrue(
        Ircv3HistoryBootstrapSuppressionPolicy.shouldSuppress(true, "*status", "ListNetworks"));
    assertFalse(Ircv3HistoryBootstrapSuppressionPolicy.shouldSuppress(true, "*status", "Version"));
  }

  @Test
  void validatesStarCursorCount() {
    assertTrue(Ircv3HistoryBootstrapSuppressionPolicy.isZncPlayStarCursorCommand("play * 20"));
    assertFalse(Ircv3HistoryBootstrapSuppressionPolicy.isZncPlayStarCursorCommand("play * nope"));
    assertFalse(Ircv3HistoryBootstrapSuppressionPolicy.isZncPlayStarCursorCommand("play #chan 20"));
  }
}
