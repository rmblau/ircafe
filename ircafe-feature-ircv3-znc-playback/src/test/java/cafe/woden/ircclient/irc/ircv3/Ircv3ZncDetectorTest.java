package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class Ircv3ZncDetectorTest {

  @Test
  void detectsZncCapabilityIncludingRemovalTokens() {
    assertTrue(Ircv3ZncDetector.seemsZncCapability("znc.in/playback"));
    assertTrue(Ircv3ZncDetector.seemsZncCapability("-ZNC.IN/server-time-iso"));
    assertFalse(Ircv3ZncDetector.seemsZncCapability("batch"));
  }

  @Test
  void detectsZncVersionInRplMyInfo() {
    assertTrue(
        Ircv3ZncDetector.seemsRpl004Znc(
            ":irc.example 004 nick irc.example ZNC-1.9.1 oiwsz biklmnopstveI"));
    assertFalse(
        Ircv3ZncDetector.seemsRpl004Znc(
            ":irc.example 004 nick irc.example InspIRCd-3.17 oiwsz biklmnopstveI"));
  }
}
