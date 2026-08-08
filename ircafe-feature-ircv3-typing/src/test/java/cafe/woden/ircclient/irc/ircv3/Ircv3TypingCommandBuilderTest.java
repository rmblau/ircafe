package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class Ircv3TypingCommandBuilderTest {

  @Test
  void normalizesTypingStateAliases() {
    assertEquals("active", Ircv3TypingCommandBuilder.normalizeState("composing"));
    assertEquals("done", Ircv3TypingCommandBuilder.normalizeState("inactive"));
    assertEquals("", Ircv3TypingCommandBuilder.normalizeState("unknown"));
  }

  @Test
  void buildsTypingTagmsg() {
    assertEquals(
        "@+typing=active TAGMSG #ircafe",
        Ircv3TypingCommandBuilder.buildRawLine("#ircafe", "composing"));
  }
}
