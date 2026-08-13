package cafe.woden.ircclient.app.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

class CommandParsePipelineTest {

  @Test
  void returnsSayForBlankAndNonSlashLines() {
    CommandParsePipeline<String> pipeline =
        new CommandParsePipeline<>(text -> "say:" + text, line -> "unknown:" + line, List.of());

    assertEquals("say:", pipeline.parse(null));
    assertEquals("say:", pipeline.parse("   "));
    assertEquals("say:hello", pipeline.parse(" hello "));
  }

  @Test
  void treatsDoubleSlashAsEscapedLeadingSlashMessageBeforeSlashParsers() {
    CommandParsePipeline<String> pipeline =
        new CommandParsePipeline<>(
            text -> "say:" + text, line -> "unknown:" + line, List.of(line -> "parsed:" + line));

    assertEquals("say:/hello", pipeline.parse("//hello"));
    assertEquals("say:/", pipeline.parse("//"));
  }

  @Test
  void triesSlashParsersInOrderUntilOneHandlesTheLine() {
    CommandParsePipeline<String> pipeline =
        new CommandParsePipeline<>(
            text -> "say:" + text,
            line -> "unknown:" + line,
            List.of(line -> null, line -> "second:" + line, line -> "third:" + line));

    assertEquals("second:/join #ircafe", pipeline.parse("/join #ircafe"));
  }

  @Test
  void ignoresNullSlashParsersAndFallsBackToUnknown() {
    List<Function<String, String>> parsers = Arrays.asList(line -> null, null);
    CommandParsePipeline<String> pipeline =
        new CommandParsePipeline<>(text -> "say:" + text, line -> "unknown:" + line, parsers);

    assertEquals("unknown:/missing", pipeline.parse("/missing"));
  }
}
