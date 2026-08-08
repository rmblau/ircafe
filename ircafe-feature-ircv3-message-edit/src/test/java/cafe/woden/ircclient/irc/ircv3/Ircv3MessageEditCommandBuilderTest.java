package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class Ircv3MessageEditCommandBuilderTest {

  @Test
  void buildsMessageEditLine() {
    assertEquals(
        "@+draft/edit=abc\\:123 PRIVMSG #ircafe :corrected text",
        Ircv3MessageEditCommandBuilder.buildRawLine(
            "#ircafe", "abc;123", " corrected text "));
  }

  @Test
  void escapesDecodedWhitespaceInTargetMessageId() {
    assertEquals(
        "@+draft/edit=abc\\s123 PRIVMSG #ircafe :corrected",
        Ircv3MessageEditCommandBuilder.buildRawLine("#ircafe", "abc 123", "corrected"));
  }
}
