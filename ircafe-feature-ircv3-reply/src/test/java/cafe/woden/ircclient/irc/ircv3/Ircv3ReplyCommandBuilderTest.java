package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class Ircv3ReplyCommandBuilderTest {

  @Test
  void buildsRawReplyLine() {
    assertEquals(
        "@+reply=abc\\:123 PRIVMSG #ircafe :hello",
        Ircv3ReplyCommandBuilder.buildRawLine("#ircafe", "abc;123", " hello "));
  }

  @Test
  void buildsInputPrefill() {
    assertEquals(
        "/quote @+reply=abc\\:123 PRIVMSG #ircafe :",
        Ircv3ReplyCommandBuilder.buildPrefillDraft("#ircafe", "abc;123"));
  }

  @Test
  void escapesDecodedWhitespaceInMessageIds() {
    assertEquals(
        "/quote @+reply=abc\\s123\\:xyz\\\\tail PRIVMSG #ircafe :",
        Ircv3ReplyCommandBuilder.buildPrefillDraft("#ircafe", "abc 123;xyz\\tail"));
  }
}
