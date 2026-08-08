package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class Ircv3InviteNotifyParserTest {

  @Test
  void parsesInviteChannelFromTrailingParameter() {
    Ircv3InviteNotifyParser.Observation observed =
        Ircv3InviteNotifyParser.parse(
                "alice",
                "INVITE",
                ":alice!u@h INVITE bob :#ircafe",
                List.of("bob"))
            .orElseThrow();

    assertEquals("alice", observed.fromNick());
    assertEquals("bob", observed.inviteeNick());
    assertEquals("#ircafe", observed.channel());
    assertEquals("", observed.reason());
  }

  @Test
  void parsesExplicitChannelAndReason() {
    Ircv3InviteNotifyParser.Observation observed =
        Ircv3InviteNotifyParser.parse(
                "alice",
                "INVITE",
                ":alice!u@h INVITE bob #ircafe :Come join us",
                List.of("bob", "#ircafe"))
            .orElseThrow();

    assertEquals("#ircafe", observed.channel());
    assertEquals("Come join us", observed.reason());
  }

  @Test
  void rejectsOtherCommandsAndMissingChannels() {
    assertTrue(
        Ircv3InviteNotifyParser.parse("alice", "PRIVMSG", "", List.of("bob", "#ircafe"))
            .isEmpty());
    assertTrue(
        Ircv3InviteNotifyParser.parse("alice", "INVITE", ":alice INVITE bob", List.of("bob"))
            .isEmpty());
  }
}
