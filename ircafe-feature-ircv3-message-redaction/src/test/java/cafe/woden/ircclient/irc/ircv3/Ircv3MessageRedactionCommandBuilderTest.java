package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class Ircv3MessageRedactionCommandBuilderTest {

  @Test
  void buildsRedactionWithAndWithoutReason() {
    assertEquals(
        "REDACT #ircafe abc",
        Ircv3MessageRedactionCommandBuilder.buildRawLine("#ircafe", "abc", ""));
    assertEquals(
        "REDACT #ircafe abc :duplicate",
        Ircv3MessageRedactionCommandBuilder.buildRawLine("#ircafe", "abc", " duplicate "));
  }
}
