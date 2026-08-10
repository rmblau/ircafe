package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class Ircv3StandardReplyParserTest {

  @Test
  void parsesStructuredStandardReply() {
    Ircv3StandardReplyParser.StandardReply reply =
        Ircv3StandardReplyParser.parse(
                "FAIL",
                List.of("CHATHISTORY", "INVALID_PARAMS", "timestamp=bad", ":Invalid selector"))
            .orElseThrow();

    assertEquals(Ircv3StandardReplyParser.Kind.FAIL, reply.kind());
    assertEquals("CHATHISTORY", reply.command());
    assertEquals("INVALID_PARAMS", reply.code());
    assertEquals("timestamp=bad", reply.context());
    assertEquals("Invalid selector", reply.description());
  }

  @Test
  void ignoresNonStandardReplyCommands() {
    assertTrue(Ircv3StandardReplyParser.parse("PRIVMSG", List.of("#ircafe", ":hello")).isEmpty());
  }
}
