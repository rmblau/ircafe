package cafe.woden.ircclient.app.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class CommandLineTokenizerTest {

  @Test
  void tokenizesWhitespaceAndQuotedArguments() {
    assertEquals(
        List.of("/filter", "add", "name", "text=hello world"),
        CommandLineTokenizer.tokenize("/filter add name \"text=hello world\""));
    assertEquals(
        List.of("/filter", "add", "name", "text=hello world"),
        CommandLineTokenizer.tokenize("/filter add name 'text=hello world'"));
  }

  @Test
  void handlesBackslashEscapes() {
    assertEquals(
        List.of("/filter", "add", "line\nnext", "tab\tvalue", "\"quoted\""),
        CommandLineTokenizer.tokenize("/filter add line\\nnext tab\\tvalue \\\"quoted\\\""));
  }

  @Test
  void rejectsDanglingEscapesAndUnterminatedQuotes() {
    assertThrows(IllegalArgumentException.class, () -> CommandLineTokenizer.tokenize("/filter \\"));
    assertThrows(
        IllegalArgumentException.class, () -> CommandLineTokenizer.tokenize("/filter 'open"));
    assertThrows(
        IllegalArgumentException.class, () -> CommandLineTokenizer.tokenize("/filter \"open"));
  }
}
