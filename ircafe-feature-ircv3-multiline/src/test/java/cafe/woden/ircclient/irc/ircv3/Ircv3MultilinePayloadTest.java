package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class Ircv3MultilinePayloadTest {

  @Test
  void exposesNormalizedLinesJoinedTextAndUtf8Size() {
    Ircv3MultilinePayload payload = Ircv3MultilinePayload.from("one\r\ntwo\n");

    assertEquals(List.of("one", "two", ""), payload.lines());
    assertEquals("one\ntwo\n", payload.joinedText());
    assertEquals(8L, payload.utf8Bytes());
    assertEquals(3, payload.lineCount());
    assertTrue(payload.isMultiline());
    assertFalse(payload.isEmpty());
  }
}
