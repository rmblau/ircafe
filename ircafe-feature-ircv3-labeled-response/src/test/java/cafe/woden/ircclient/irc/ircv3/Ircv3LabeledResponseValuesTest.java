package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class Ircv3LabeledResponseValuesTest {

  @Test
  void generatesCompactServerScopedLabels() {
    assertEquals(
        "ircafe-liberachat-9ix",
        Ircv3LabeledResponseValues.generateClientLabel(" Libera.Chat ", 12345));
    assertEquals("ircafe-srv-1", Ircv3LabeledResponseValues.generateClientLabel("!!!", 1));
  }

}
