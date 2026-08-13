package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class Ircv3ChghostParserTest {

  @Test
  void parsesHostChange() {
    Ircv3ChghostParser.ChangeHost change =
        Ircv3ChghostParser.parse("CHGHOST", List.of("newuser", "new.host")).orElseThrow();

    assertEquals("alice!newuser@new.host", change.hostmaskFor("alice").orElseThrow());
  }

  @Test
  void rejectsOtherCommandsAndIncompleteChanges() {
    assertTrue(Ircv3ChghostParser.parse("SETNAME", List.of("newuser", "new.host")).isEmpty());
    assertTrue(Ircv3ChghostParser.parse("CHGHOST", List.of("newuser")).isEmpty());
  }
}
