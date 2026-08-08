package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class Ircv3HistoryBatchControlParserTest {

  @Test
  void parsesChatHistoryBatchStart() {
    Ircv3HistoryBatchControlParser.Result result =
        Ircv3HistoryBatchControlParser.parse(
            ":server.example BATCH +abc draft/chathistory #ircafe");

    assertTrue(result.batchCommand());
    Ircv3HistoryBatchControlParser.Start start =
        assertInstanceOf(Ircv3HistoryBatchControlParser.Start.class, result.control());
    assertEquals("abc", start.batchId());
    assertEquals("draft/chathistory", start.type());
    assertEquals("#ircafe", start.target());
    assertTrue(start.isChatHistory());
  }

  @Test
  void usesTrailingTargetAndParsesEnd() {
    Ircv3HistoryBatchControlParser.Start start =
        assertInstanceOf(
            Ircv3HistoryBatchControlParser.Start.class,
            Ircv3HistoryBatchControlParser.parse(
                    ":server.example BATCH +abc chathistory :#ircafe")
                .control());
    Ircv3HistoryBatchControlParser.End end =
        assertInstanceOf(
            Ircv3HistoryBatchControlParser.End.class,
            Ircv3HistoryBatchControlParser.parse(":server.example BATCH -abc").control());

    assertEquals("#ircafe", start.target());
    assertEquals("abc", end.batchId());
  }

  @Test
  void distinguishesMalformedBatchFromOtherCommands() {
    Ircv3HistoryBatchControlParser.Result malformed =
        Ircv3HistoryBatchControlParser.parse(":server.example BATCH");
    Ircv3HistoryBatchControlParser.Result other =
        Ircv3HistoryBatchControlParser.parse(":server.example PRIVMSG #ircafe :hello");

    assertTrue(malformed.batchCommand());
    assertNull(malformed.control());
    assertTrue(!other.batchCommand());
  }
}
