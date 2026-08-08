package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class Ircv3ReactionCommandBuilderTest {

  @Test
  void buildsReactionAndRemovalLines() {
    assertEquals(
        "@+draft/react=:+1:;+reply=abc\\:123 TAGMSG #ircafe",
        Ircv3ReactionCommandBuilder.buildReactRawLine("#ircafe", "abc;123", ":+1:"));
    assertEquals(
        "@+draft/unreact=:+1:;+reply=abc\\:123 TAGMSG #ircafe",
        Ircv3ReactionCommandBuilder.buildUnreactRawLine("#ircafe", "abc;123", ":+1:"));
  }

  @Test
  void buildsDefaultReactionPrefill() {
    assertEquals(
        "/quote @+draft/react=:+1:;+reply=abc TAGMSG #ircafe",
        Ircv3ReactionCommandBuilder.buildReactPrefillDraft("#ircafe", "abc"));
  }

  @Test
  void escapesDecodedWhitespaceInTagValues() {
    assertEquals(
        "@+draft/react=two\\swords;+reply=abc\\s123 TAGMSG #ircafe",
        Ircv3ReactionCommandBuilder.buildReactRawLine("#ircafe", "abc 123", "two words"));
  }
}
