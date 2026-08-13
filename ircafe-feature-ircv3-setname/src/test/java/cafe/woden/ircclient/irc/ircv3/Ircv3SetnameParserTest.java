package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class Ircv3SetnameParserTest {

  @Test
  void parsesRealName() {
    Ircv3SetnameParser.SetName setName =
        Ircv3SetnameParser.parse("SETNAME", List.of(":Alice Example")).orElseThrow();

    assertEquals("Alice Example", setName.realName());
  }

  @Test
  void rejectsOtherCommandsAndBlankNames() {
    assertTrue(Ircv3SetnameParser.parse("CHGHOST", List.of(":Alice Example")).isEmpty());
    assertTrue(Ircv3SetnameParser.parse("SETNAME", List.of("")).isEmpty());
  }
}
